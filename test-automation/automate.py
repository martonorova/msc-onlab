import os
import requests
import urllib

WORKER_BUSY_THREADS_QUERY="sum(worker_busy_threads{job='kubernetes-service-endpoints'})"
WORKER_PODS_COUNT_QUERY="kube_deployment_status_replicas{deployment='kubedepend-worker-depl'}"
NEEDED_WORKER_RATIO_QUERY="(1 + sum by (namespace)(worker_busy_threads{job='worker-pods'})) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})"
QUEUE_SIZE_QUERY="org_apache_activemq_Broker_QueueSize{destinationName=~'jobWorker.*', job='activemq'}"
# Prometheus HOST
PROMETHEUS_QUERY_URL="http://localhost:9090/api/v1/query?query="


WORKER_BUSY_THREADS_COUNT="(curl --silent {PROMETHEUS_QUERY_URL}$( urlencode "{WORKER_BUSY_THREADS_QUERY}" )" | jq .data.result[0].value[1])"


QUEUE_SIZE=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${QUEUE_SIZE_QUERY}" )" | jq .data.result[0].value[1])
WORKER_PODS_COUNT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${WORKER_PODS_COUNT_QUERY}" )" | jq .data.result[0].value[1])
NEEDED_WORKER_RATIO=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${NEEDED_WORKER_RATIO_QUERY}" )" | jq .data.result[0].value[1])