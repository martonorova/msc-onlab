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

import constants as c
from model import BackendDependabilityMetrics
from model import SystemState

logging.basicConfig(
    level=logging.INFO
)

# setup Locust logging
setup_logging('DEBUG', None)

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
            'input': 50
        }
        self.client.post('/api/v1/jobs', json=data, headers=headers)


def main():

    args = [arg for arg in sys.argv[1:]]
    for i, arg in enumerate(args):
        print(i, arg)

    wait_for_stable_state()

    # Setup Locust objects

    # setup Environment and Runner
    env = Environment(user_classes=[User])
    env.create_local_runner()

    # start a greenlet that periodically outputs the current stats
    gevent.spawn(stats_printer(env.stats))

    # start a greenlet that save current stats to history
    gevent.spawn(stats_history, env.runner)

    logging.info('Creating chaos objects...')
    os.system('pwd')

    logging.info('Chaos objects applied.')

    logging.info('Generating load...')

    # start the test
    env.runner.start(user_count=10, spawn_rate=1)

    # in 60 seconds stop the runner
    gevent.spawn_later(60, lambda: env.runner.quit())

    # wait for the greenlets
    env.runner.greenlet.join()

    logging.info('Load generation finished')

    # get dependability metrics
    

    # save dependability metrics

    logging.info('Deleting chaos objects...')

    logging.info('Chaos objects deleted.')







# Queries Prometheus with the given Prometheus format query
def query_prometheus(query):
    # assemble url
    url = f'{c.PROMETHEUS_HOST}{c.PROMETHEUS_QUERY_ENDPOINT}?query={urllib.parse.quote(query)}'
    # get data
    res = requests.get(url=url)
    # print(res.json())
    value = res.json()['data']['result'][0]['value'][1]
    # print(value)

    # values is string originally (comes from JSON)
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

def get_dependability_metrics():
    metrics = BackendDependabilityMetrics()

    

if __name__ == "__main__":
    main()