#!/bin/bash


# variables used in the queries
RANGE=30m
INTERVAL=15s

### Prometheus query variables ###
BACKEND_AVAILABILITY_QUERY="avg_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${INTERVAL}])"

BACKEND_UNAVAILABILITY_QUERY="(1 - sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${INTERVAL}])) / count_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${INTERVAL}])"

BACKEND_MUT_QUERY="15 * sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) / (floor((changes(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) + 1 + probe_success{instance=~'.*kubedepend-backend.*'} offset ${RANGE}) / 2))"

BACKEND_MDT_QUERY="15 * (count_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) - sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}])) / (floor((changes(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) + 2 - probe_success{instance=~'.*kubedepend-backend.*'} offset ${RANGE}) / 2))"

BACKEND_MTBF_QUERY="${BACKEND_MUT_QUERY} + ${BACKEND_MDT_QUERY}"

WORKER_BUSY_THREADS_QUERY="sum(worker_busy_threads{job='kubernetes-service-endpoints'})"

WORKER_PODS_COUNT_QUERY="kube_deployment_status_replicas{deployment='kubedepend-worker-depl'}"

NEEDED_WORKER_RATIO_QUERY="(1 + sum by (namespace)(worker_busy_threads{job='worker-pods'})) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})"

QUEUE_SIZE_QUERY="org_apache_activemq_Broker_QueueSize{destinationName=~'jobWorker.*', job='activemq'}"




# echo "Backend availability query: ${BACKEND_AVAILABILITY_QUERY}"
# echo "Backend unavailability query: ${BACKEND_UNAVAILABILITY_QUERY}"
# echo "Backend MUT query: ${BACKEND_MUT_QUERY}"
# echo "Backend MDT query: ${BACKEND_MDT_QUERY}"
# echo "Backend MTBF query: ${BACKEND_MTBF_QUERY}"
# echo "Worker busy threads query: ${WORKER_BUSY_THREADS_QUERY}"
# echo "Needed worker ratio query: ${NEEDED_WORKER_RATIO_QUERY}"
# echo "Queue siye query: ${QUEUE_SIZE_QUERY}"

# Open backend port on localhost
# currently by hand

# Check if system is in a stable state (busy worker = 0, queue size = 0, worker-pods = 1, needed worker ration = 1)

# apply chaos files
echo "Creating chaos objects..."

kubectl apply -f "$(pwd)/chaos-files"

echo "Chaos objects created."

echo "Testing in progress..."
SECONDS=0

# artillery run ../artillery/submit_jobs.yaml

echo "Load sent, waiting for jobs to finish..."

sleep 2

echo "Jobs finished in $SECONDS seconds."

# delete chaos files
echo "Deleting chaos objects..."

kubectl delete -f "$(pwd)/chaos-files"

echo "Chaos objects deleted."

echo "Elapsed time is"

echo "Get results..."
echo "Range = __, Interval = __"

echo "Backend availability"
# prometheus query
# curl 'http://localhost:9090/api/v1/query?query=avg_over_time%28probe_success%7Binstance%3D~%22.%2Akubedepend-backend.%2A%22%7D%5B15s%5D%29' | jq .data.result[0].value[1]

echo "Backend unavailability -"