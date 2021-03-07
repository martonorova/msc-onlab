import requests
import locust

import logging
import urllib.parse
import time
import os
import sys

import constants as c

logging.basicConfig(
    level=logging.INFO
)


def main():

    args = [arg for arg in sys.argv[1:]]
    for i, arg in enumerate(args):
        print(i, arg)

    wait_for_stable_state()

    logging.info('Creating chaos objects...')
    os.system('pwd')

    logging.info('Chaos objects applied.')

    logging.info('Generating load...')










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

if __name__ == "__main__":
    main()