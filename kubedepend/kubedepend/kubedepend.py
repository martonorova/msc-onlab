import requests
from locust import HttpUser, task, constant_pacing
from locust.env import Environment
from locust.stats import stats_history, stats_printer
from locust.log import setup_logging
import gevent

import logging
import urllib.parse
import time
import os
import sys
from datetime import datetime

import constants as c
from model import BackendDependabilityMetrics
from model import SystemState
from model import TestResult

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
            'input': 49
        }
        self.client.post('/api/v1/jobs', json=data, headers=headers)


def main():

    # args = [arg for arg in sys.argv[1:]]
    # for i, arg in enumerate(args):
    #     print(i, arg)


    logging.info('Waiting for stable system state...')
    wait_for_stable_state()

    result = TestResult()

    for i in range(10):

        duration = 600 # sec

        # Setup Locust objects

        # setup Environment and Runner
        env = Environment(user_classes=[User])
        env.create_local_runner()

        # start a greenlet that periodically outputs the current stats
        gevent.spawn(stats_printer(env.stats))

        # start a greenlet that save current stats to history
        gevent.spawn(stats_history, env.runner)

        logging.info('Creating chaos objects...')

        os.system('''
        helm upgrade \
            --install \
            --namespace chaos-testing \
            -f ../charts/kubedepend-chaos/values.yaml \
            kubedepend-chaos \
            ../charts/kubedepend-chaos \
            --set podChaos.enabled=true
        ''')

        logging.info('Chaos objects applied.')

        logging.info('Generating load...')

        # start the test
        env.runner.start(user_count=1, spawn_rate=1)

        # in 60 seconds stop the runner
        gevent.spawn_later(duration, lambda: env.runner.quit())

        # wait for the greenlets
        env.runner.greenlet.join()

        logging.info('Load generation finished')

        # get dependability metrics
        
        metrics = get_dependability_metrics(duration)
        
        result.add_metric(metrics)

        # save dependability metrics

        # save chaos configuration
        # os.system('''
        #     helm get manifest kubedepend-chaos -n chaos-testing
        # ''')

        logging.info('Deleting chaos objects...')

        os.system('''
            helm delete kubedepend-chaos -n chaos-testing
        ''')

        logging.info('Chaos objects deleted.')

    result.save_results(f'results/results-{datetime.now().strftime("%m-%d-%Y_%H-%M-%S")}.csv')

    logging.info('Test finished')



# Queries Prometheus with the given Prometheus format query
def query_prometheus(query, trycount=0):
    # assemble url
    url = f'{c.PROMETHEUS_HOST}{c.PROMETHEUS_QUERY_ENDPOINT}?query={urllib.parse.quote(query)}'
    # get data
    res = requests.get(url=url)
    # print(res.json())
    value = None
    try:
        value = res.json()['data']['result'][0]['value'][1]
    except IndexError:
        if trycount >= 10:
            raise ValueError('No data from Prometheus')
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
        logging.info(f'There are still busy worker threads ({worker_busy_threads_count})')
        return False
    
    if queue_size != 0:
        logging.info(f'Queue is not empty ({queue_size} jobs waiting)')
        return False

    if worker_pods_count != 1:
        logging.info(f'There are multiple worker pods ({worker_pods_count})')
        return False
    
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

    metrics = BackendDependabilityMetrics()

    metrics.availability = query_prometheus(c.backend_availability_query(range_length))
    metrics.mut = query_prometheus(c.backend_availability_query(range_length))
    metrics.mdt = query_prometheus(c.backend_mdt_query(range_length))
    metrics.mtbf = query_prometheus(c.backend_mtbf_query(range_length))

    return metrics


    

if __name__ == "__main__":
    main()