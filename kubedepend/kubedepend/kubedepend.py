import requests
from locust import HttpUser, task, constant_pacing
from locust.env import Environment
from locust.stats import stats_history, stats_printer
from locust.log import setup_logging
import gevent
import click
from mysql.connector import connect, Error

import logging
import urllib.parse
import time
import os
import sys
from datetime import datetime
import tarfile
import pathlib
import subprocess

import constants as c
from model import MeasurementResult
from model import BackendMetrics
from model import SystemState
from model import MeasurementSequenceResult

logging.basicConfig(
    level=logging.INFO
)

# setup Locust logging
setup_logging('INFO', None)


class User(HttpUser):
    # constant_pacing for an adaptive time that ensures the task runs (at most) once every X seconds
    wait_time = constant_pacing(5)
    host = c.BACKEND_HOST

    @task
    def submit_job_task(self):
        headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
        data = {
            'input': 48
        }
        self.client.post('/api/v1/jobs', json=data, headers=headers)

HELM_COMMAND_FIX_PART = [
    'helm',
    'upgrade',
    '--install',
    '--namespace',
    'chaos-testing',
    'kubedepend-chaos',
    '../charts/kubedepend-chaos'
]


@click.command()
@click.option('--nosave', is_flag=True)
@click.option('--fault-profile', type=click.Choice(list(c.FAULT_PROFILES.keys())), default='none', help='Name of the fault profile')
@click.option('--measurement-count', type=click.INT, default=10, help='Number of measurements to make during the measurement sequence')
@click.option('--load-duration', type=click.INT, default=600, help='Duration of the load generation in a single measurement in SECONDS')
@click.option('--cluster-type', type=click.Choice(['minikube', 'eks']), default='minikube', help='Type of the K8s cluster the stack runs on')
@click.option('--locust_user_count', type=click.INT, default=1, help='Total number of Locust users to start')
@click.option('--locust_spawn_rate', type=click.INT, default=1, help='Number of Locust users to spawn per second')
@click.option('--comment', type=click.STRING, help='Give a comment about the measurement sequence')
def main(nosave, fault_profile, measurement_count, load_duration, locust_user_count, locust_spawn_rate, cluster_type, comment):
    click.echo('nosave=' + str(nosave))
    click.echo('fault_profile=' + str(fault_profile))
    click.echo('measurement_count=' + str(measurement_count))
    click.echo('load_duration=' + str(load_duration))
    click.echo('cluster_type=' + str(cluster_type))
    click.echo('locust_user_count=' + str(locust_user_count))
    click.echo('locust_spawn_rate=' + str(locust_spawn_rate))
    click.echo('comment=' + str(comment))

    check_working_dir()

    # Save the start time of the measurement sequence
    start_time = datetime.now().strftime("%m-%d-%Y_%H-%M-%S.%f")

    # lint helm chart
    try:
        logging.info('Linting Helm chart...')
        subprocess.check_output(['helm', 'lint', '../charts/kubedepend-chaos'])
        logging.info('Linting Helm chart finished OK')
    except subprocess.CalledProcessError as error:
        logging.error('Helm lint failed, exiting...')
        exit()

    # assembling helm value options according to fault profile
    helm_value_sets = assemble_helm_set_options(fault_profile)

    # filter out emtpy strings
    helm_command = [x for x in HELM_COMMAND_FIX_PART + helm_value_sets if x]

    # Save current stack into archive
    if not nosave:
        save_helm_chart(helm_command=helm_command)
        archive_stack(start_time)

    sequence_result = MeasurementSequenceResult(
        start_time=start_time,
        fault_profile=fault_profile,
        cluster_type=cluster_type,
        load_duration=load_duration,
        locust_user_count=locust_user_count,
        locust_spawn_rate=locust_spawn_rate,
        comment=comment)

    for i in range(measurement_count):

        logging.info(f'Start measurement #{i + 1}')

        logging.info('Waiting for stable system state...')
        wait_for_stable_state()

        # Initialize measurement result
        measurement_result = MeasurementResult()

        # Setup Locust objects

        # setup Environment and Runner
        env = Environment(user_classes=[User])
        env.create_local_runner()

        # start a greenlet that periodically outputs the current stats
        gevent.spawn(stats_printer(env.stats))

        # start a greenlet that save current stats to history
        gevent.spawn(stats_history, env.runner)

        logging.info('Creating chaos objects...')

        subprocess.run(helm_command)

        logging.info('Chaos objects applied.')

        logging.info('Generating load...')

        # start the test
        env.runner.start(user_count=locust_user_count,
                         spawn_rate=locust_spawn_rate)

        # in 'duartion' seconds stop the runner
        gevent.spawn_later(load_duration, lambda: env.runner.quit())

        # wait for the greenlets
        env.runner.greenlet.join()

        logging.info('Load generation finished')

        # get dependability metrics

        metrics = get_dependability_metrics(load_duration)

        # add metrics to measurement result
        measurement_result.backend_metrics = metrics
        # end measurement (fill end_time attribute)
        measurement_result.end()
        

        # save dependability metrics

        logging.info('Deleting chaos objects...')

        subprocess.run(['helm', 'delete', 'kubedepend-chaos', '-n', 'chaos-testing'])

        logging.info('Chaos objects deleted.')

        logging.info('Waiting for stable system state (end)...')
        wait_for_stable_state()

        # get finished jobs in database
        (measurement_result.submitted_jobs, measurement_result.finished_jobs) = get_jobs_summary()

        # add measurement result to sequence result
        sequence_result.add_measurement_result(measurement_result)

    if not nosave:
        sequence_result.save_results('results/results.csv')

    # TODO stop running greenlets (stats)

    logging.info('Test finished')


# Queries Prometheus with the given Prometheus format query
def query_prometheus(query, trycount=0):
    # assemble url
    url = f'{c.PROMETHEUS_HOST}{c.PROMETHEUS_QUERY_ENDPOINT}?query={urllib.parse.quote(query)}'
    # get data
    try:
        res = requests.get(url=url)
    except requests.exceptions.ConnectionError:
        if trycount >= 10:
            raise ValueError('Connection ERROR')
        # wait 1 sec with next attempt
        time.sleep(1)
        return query_prometheus(query, trycount=trycount + 1)

    # print(res.json())
    value = None
    try:
        value = res.json()['data']['result'][0]['value'][1]
    except IndexError:
        if trycount >= 10:
            # TODO, no data can occur also, when there is no downtime of the APP
            raise ValueError('No data from Prometheus for query: ' + str(query))
        # wait 1 sec with next attempt
        time.sleep(1)
        return query_prometheus(query, trycount=trycount + 1)

    else:
        # values is string originally (comes from JSON)
        # print(value)
        return float(value)


def is_stable_state():
    worker_busy_threads_count = query_prometheus(c.WORKER_BUSY_THREADS_QUERY)
    queue_size = query_prometheus(c.QUEUE_SIZE_QUERY)
    worker_pods_count = query_prometheus(c.WORKER_PODS_COUNT_QUERY)
    needed_worker_ratio = query_prometheus(c.NEEDED_WORKER_RATIO_QUERY)

    if worker_busy_threads_count != 0:
        logging.info(
            f'There are still busy worker threads ({worker_busy_threads_count})')
        return False

    if queue_size != 0:
        logging.info(f'Queue is not empty ({queue_size} jobs waiting)')
        return False

    if worker_pods_count != 1:
        logging.info(f'There are multiple worker pods ({worker_pods_count})')
        return False

    # TODO check if backend and db are running

    return True


def wait_for_stable_state():
    while not is_stable_state():
        logging.info('State NOT OK, waiting...')
        time.sleep(5)
    logging.info('State OK')


def generate_load():
    pass


def get_dependability_metrics(range_length):
    logging.info(f'"range_length" is: {range_length}')

    metrics = BackendMetrics()

    metrics.availability = query_prometheus(
        c.backend_availability_query(range_length))
    metrics.mut = query_prometheus(c.backend_mut_query(range_length))
    metrics.mdt = query_prometheus(c.backend_mdt_query(range_length))
    metrics.mtbf = query_prometheus(c.backend_mtbf_query(range_length))

    return metrics


def check_working_dir():
    # Check if in right working dir
    actual_working_dir = '/'.join(str(pathlib.Path.cwd()).split('/')[-2:])
    expected_working_dir = 'kubedepend/kubedepend'

    if (actual_working_dir != expected_working_dir):
        raise RuntimeError(f'Working directory must be {expected_working_dir}')


def archive_stack(datestring):
    with tarfile.open(f'results/archive-{datestring}.tgz', 'w:gz') as tar:
        tar.add(c.PATH_TO_STACK_REPO, arcname=os.path.basename(c.PATH_TO_STACK_REPO),
                filter=archive_filter)


def archive_filter(tarinfo):
    file_path = tarinfo.name.split('/')
    EXCLUDES = ['node_modules', '.gradle', 'build', 'archives', '.git', '.vscode']

    if any(item in file_path for item in EXCLUDES):
        return None
    return tarinfo


def save_helm_chart(helm_command):
    chaos_objects = subprocess.check_output(
        helm_command + ['--dry-run']
    ).decode('ascii').strip()

    last_chaos_file = pathlib.Path('results/chaos_manifest.txt')
    if last_chaos_file.exists():
        last_chaos_file.unlink()

    with open(last_chaos_file, mode='w') as chaos_file:
        chaos_file.write(chaos_objects)

def assemble_helm_set_options(fault_profile):

    helm_value_sets = []

    for chaos_object in c.FAULT_PROFILES[fault_profile]:
        # enable chaos object
        helm_value_sets.extend(['--set', f'{chaos_object["chaos"]}.enabled=true'])
        # set level of strength
        helm_value_sets.extend(['--set', f'{chaos_object["chaos"]}.strength={chaos_object["strength"]}'])

    return helm_value_sets

def get_jobs_summary():

    proc = subprocess.Popen(['kubectl -n kubedepend port-forward service/kubedepend-db 3306:3306'], shell=True)
    time.sleep(5)

    count_submitted_jobs_query = "SELECT COUNT(id) from job;"
    count_finished_jobs_query = "SELECT COUNT(id) from job WHERE result = NULL;"
    clear_table_jobs_query = "DELETE FROM job;"

    submitted_jobs = None
    finished_jobs = None

    try:
        with connect(
            host=c.DB_HOST,
            port=c.DB_PORT,
            user=c.DB_USER,
            password=c.DB_PASS,
            database=c.DB_NAME
        ) as connection:
            with connection.cursor() as cursor:
                cursor.execute(count_submitted_jobs_query)
                for row in cursor.fetchall():
                    submitted_jobs = row[0]

                cursor.execute(count_finished_jobs_query)
                for row in cursor.fetchall():
                    finished_jobs = row[0]

                cursor.execute(clear_table_jobs_query)

    except Error as e:
        print(e)
        print("Could not get Job summary from database")

    proc.terminate()

    return (submitted_jobs, finished_jobs)

if __name__ == "__main__":
    main()
