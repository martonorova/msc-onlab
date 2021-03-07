### Prometheus host
PROMETHEUS_HOST = "http://localhost:9090"
### Prometheus query endpoint
PROMETHEUS_QUERY_ENDPOINT = "/api/v1/query"

### Prometheus queries
WORKER_BUSY_THREADS_QUERY = "sum(worker_busy_threads{job='kubernetes-service-endpoints'})"
WORKER_PODS_COUNT_QUERY = "kube_deployment_status_replicas{deployment='kubedepend-worker-depl'}"
NEEDED_WORKER_RATIO_QUERY = "(1 + sum by (namespace)(worker_busy_threads{job='worker-pods'})) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})"
QUEUE_SIZE_QUERY = "org_apache_activemq_Broker_QueueSize{destinationName=~'jobWorker.*', job='activemq'}"


def backend_availability_query(range_length):
    """[summary]

    Args:
        range_length ([int]): [Range in seconds]
    """
    if type(range_length) is not int:
        raise TypeError('range_length must be int')
    return f'avg_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{range_length}])'

def backend_unavailability_query(interval):
    if type(interval) is not int:
        raise TypeError('interval must be int')

    return f'(1 - sum_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{interval}])) / count_over_time(probe_success{{instance=~".*kubedepend-backend.*"}}[{interval}])'

BACKEND_UNAVAILABILITY_QUERY="(1 - sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${INTERVAL}])) / count_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${INTERVAL}])"

BACKEND_MUT_QUERY="15 * sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) / (floor((changes(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) + 1 + probe_success{instance=~'.*kubedepend-backend.*'} offset ${RANGE}) / 2))"

BACKEND_MDT_QUERY="15 * (count_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) - sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}])) / (floor((changes(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) + 2 - probe_success{instance=~'.*kubedepend-backend.*'} offset ${RANGE}) / 2))"

BACKEND_MTBF_QUERY="${BACKEND_MUT_QUERY} + ${BACKEND_MDT_QUERY}"


### Backend host
BACKEND_HOST = "http://localhost:8080"