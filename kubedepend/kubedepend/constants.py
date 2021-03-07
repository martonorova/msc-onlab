### Prometheus host
PROMETHEUS_HOST = "http://localhost:9090"
### Prometheus query endpoint
PROMETHEUS_QUERY_ENDPOINT = "/api/v1/query"

### Prometheus queries
WORKER_BUSY_THREADS_QUERY = "sum(worker_busy_threads{job='kubernetes-service-endpoints'})"
WORKER_PODS_COUNT_QUERY = "kube_deployment_status_replicas{deployment='kubedepend-worker-depl'}"
NEEDED_WORKER_RATIO_QUERY = "(1 + sum by (namespace)(worker_busy_threads{job='worker-pods'})) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})"
QUEUE_SIZE_QUERY = "org_apache_activemq_Broker_QueueSize{destinationName=~'jobWorker.*', job='activemq'}"

### Backend host
BACKEND_HOST = "http://localhost:8080"