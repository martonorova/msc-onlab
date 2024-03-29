import os

print(os.getenv('USE_KAFKA'))

# Prometheus host
PROMETHEUS_HOST = "http://localhost:9090"
# Prometheus query endpoint
PROMETHEUS_QUERY_ENDPOINT = "/api/v1/query"

# Prometheus queries
WORKER_BUSY_THREADS_QUERY = "sum(worker_busy_threads{job='kubernetes-service-endpoints'}) or absent(sum(worker_busy_threads{job='kubernetes-service-endpoints'})) - 1"

WORKER_PODS_COUNT_QUERY = "kube_deployment_status_replicas{deployment='kubedepend-worker-depl'}"

NEEDED_WORKER_RATIO_QUERY = "(1 + (kafka_consumergroup_lag_sum{consumergroup='worker',topic='jobWorkerTopic'} or absent(kafka_consumergroup_lag_sum{consumergroup='worker',topic='jobWorkerTopic'}) - 1) + on(namespace) (( sum by (namespace) (worker_busy_threads{job='worker-pods'} )) or label_replace(vector(0), 'namespace', 'kubedepend', '', ''))) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})" if os.getenv('USE_KAFKA') == 'true' else "(1 + (org_apache_activemq_Broker_QueueSize{destinationName='jobWorkerQueue', job='activemq'} or absent(org_apache_activemq_Broker_QueueSize{destinationName='jobWorkerQueue', job='activemq', namespace='kubedepend'}) - 1) + on(namespace) (( sum by (namespace) (worker_busy_threads{job='worker-pods'} )) or label_replace(vector(0), 'namespace', 'kubedepend', '', ''))) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})"

"(1 + (kafka_consumergroup_lag_sum{consumergroup='worker',topic='jobWorkerTopic'} or absent(kafka_consumergroup_lag_sum{consumergroup='worker',topic='jobWorkerTopic'}) - 1) + on(namespace) (( sum by (namespace) (worker_busy_threads{job='worker-pods'} )) or label_replace(vector(0), 'namespace', 'kubedepend', '', ''))) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})"

QUEUE_SIZE_QUERY = "org_apache_activemq_Broker_QueueSized{destinationName=~'jobWorker.*', job='activemq'} or absent(org_apache_activemq_Broker_QueueSized{destinationName=~'jobWorker.*', job='activemq'}) - 1"

DATABASE_DEPLOYMENT_READY_REPLICAS_QUERY = "kube_deployment_status_replicas_ready{namespace='kubedepend', deployment='kubedepend-db-depl'}"

BACKEND_DEPLOYMENT_READY_REPLICAS_QUERY = "kube_deployment_status_replicas_ready{namespace='kubedepend', deployment='kubedepend-backend-depl'}"


def backend_availability_query(range_length):
    """[summary]

    Args:
        range_length ([int]): [Range in seconds]
    """
    if type(range_length) is not int:
        raise TypeError('range_length must be int')
    return f'avg_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{range_length}s])'


def backend_unavailability_query(interval):
    if type(interval) is not int:
        raise TypeError('interval must be int')

    return f'(1 - sum_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{interval}s])) / count_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{interval}s])'


def backend_mut_query(range_length):
    if type(range_length) is not int:
        raise TypeError('range_length must be int')

    return f'15 * sum_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{range_length}s]) / (floor((changes(probe_success{{instance=~".*kubedepend-backend.*"}}[{range_length}s]) + 1 + probe_success{{instance=~".*kubedepend-backend.*"}} offset {range_length}s) / 2))'


def backend_mdt_query(range_length):
    if type(range_length) is not int:
        raise TypeError('range_length must be int')

    return f'15 * (count_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{range_length}s]) - sum_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{range_length}s])) / (floor((changes(probe_success{{instance=~".*kubedepend-backend.*"}}[{range_length}s]) + 2 - probe_success{{instance=~".*kubedepend-backend.*"}} offset {range_length}s) / 2))'


def backend_mtbf_query(range_length):
    if type(range_length) is not int:
        raise TypeError('range_length must be int')

    return f'{backend_mut_query(range_length)} + {backend_mdt_query(range_length)}'


# Backend host
BACKEND_HOST = "http://localhost:8000"

# Database host
DB_HOST = "127.0.0.1"
DB_PORT = "3306"
DB_USER = "jobsuser"
DB_PASS = "userpass"
DB_NAME = "jobs"

FAULT_PROFILES = {
    'custom': [{'chaos': 'ioChaos', 'strength': 'medium'}, {'chaos': 'podFailureChaos', 'strength': 'medium'}],
    'io': [{'chaos': 'ioChaos', 'strength': 'medium'}],
    'network-delay': [{'chaos': 'networkDelayChaos', 'strength': 'medium'}],
    'network-partition': [{'chaos': 'networkPartitionChaos', 'strength': 'medium'}],
    'pod-failure': [{'chaos': 'podFailureChaos', 'strength': 'medium'}],
    'pod-kill': [{'chaos': 'podKillChaos', 'strength': 'medium'}],
    'stress-cpu': [{'chaos': 'stressCpuChaos', 'strength': 'medium'}],
    'stress-mem': [{'chaos': 'stressMemChaos', 'strength': 'medium'}],
    'none': []
}

PATH_TO_STACK_REPO = os.getenv('WORKSPACE', '../../msc-onlab')
