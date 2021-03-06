#!/bin/bash

### Metrics query (which are present on Grafana)
# shellcheck disable=SC2089
WORKER_BUSY_THREADS_QUERY="sum(worker_busy_threads{job='kubernetes-service-endpoints'})"

WORKER_PODS_COUNT_QUERY="kube_deployment_status_replicas{deployment='kubedepend-worker-depl'}"

NEEDED_WORKER_RATIO_QUERY="(1 + sum by (namespace)(worker_busy_threads{job='worker-pods'})) / on(namespace) (kube_deployment_status_replicas{deployment='kubedepend-worker-depl'})"

# shellcheck disable=SC2089
QUEUE_SIZE_QUERY="org_apache_activemq_Broker_QueueSize{destinationName=~'jobWorker.*', job='activemq'}"

# echo "Backend availability query: ${BACKEND_AVAILABILITY_QUERY}"
# echo "Backend unavailability query: ${BACKEND_UNAVAILABILITY_QUERY}"
# echo "Backend MUT query: ${BACKEND_MUT_QUERY}"
# echo "Backend MDT query: ${BACKEND_MDT_QUERY}"
# echo "Backend MTBF query: ${BACKEND_MTBF_QUERY}"
# echo "Worker busy threads query: ${WORKER_BUSY_THREADS_QUERY}"
# echo "Needed worker ratio query: ${NEEDED_WORKER_RATIO_QUERY}"
# echo "Queue siye query: ${QUEUE_SIZE_QUERY}"

# Prometheus HOST
PROMETHEUS_QUERY_URL="http://localhost:9090/api/v1/query?query="
# echo ${PROMETHEUS_QUERY_URL}
# echo "${PROMETHEUS_QUERY_URL}$( urlencode ${WORKER_BUSY_THREADS_QUERY} )"


WORKER_BUSY_THREADS_COUNT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${WORKER_BUSY_THREADS_QUERY}" )" | jq .data.result[0].value[1])

QUEUE_SIZE=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${QUEUE_SIZE_QUERY}" )" | jq .data.result[0].value[1])

WORKER_PODS_COUNT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${WORKER_PODS_COUNT_QUERY}" )" | jq .data.result[0].value[1])

NEEDED_WORKER_RATIO=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${NEEDED_WORKER_RATIO_QUERY}" )" | jq .data.result[0].value[1])
# echo ${WORKER_BUSY_THREADS_COUNT}

### Open backend port on localhost ###
# currently by hand

# Check if system is in a stable state (busy worker = 0, queue size = 0, worker-pods = 1, needed worker ration = 1)

# wait for system to be in a stable stat
while ! [[ $WORKER_BUSY_THREADS_COUNT =~ "0" ]] || ! [[ $QUEUE_SIZE =~ "0" ]] || ! [[ $WORKER_PODS_COUNT =~ "1" ]] || ! [[ $NEEDED_WORKER_RATIO =~ "1" ]]; do
    echo "State NOT OK..."
    sleep 5

    WORKER_BUSY_THREADS_COUNT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${WORKER_BUSY_THREADS_QUERY}" )" | jq .data.result[0].value[1])
    QUEUE_SIZE=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${QUEUE_SIZE_QUERY}" )" | jq .data.result[0].value[1])
    WORKER_PODS_COUNT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${WORKER_PODS_COUNT_QUERY}" )" | jq .data.result[0].value[1])
    NEEDED_WORKER_RATIO=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${NEEDED_WORKER_RATIO_QUERY}" )" | jq .data.result[0].value[1])

done

echo "State OK, start test..."

# apply chaos files
echo "Creating chaos objects..."

kubectl apply -f "$(pwd)/chaos-files"

echo "Chaos objects created."

echo "Testing in progress..."
SECONDS=0

artillery run ../artillery/submit_jobs.yaml

# sleep 2

echo "Load sent, waiting for jobs to finish..."

sleep 20

 
QUEUE_SIZE=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${QUEUE_SIZE_QUERY}" )" | jq .data.result[0].value[1])
WORKER_BUSY_THREADS_COUNT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${WORKER_BUSY_THREADS_QUERY}" )" | jq .data.result[0].value[1])

# wait for busy threads == 0 and queue size == 0
while ! { [[ $WORKER_BUSY_THREADS_COUNT =~ "0" ]] && [[ $QUEUE_SIZE =~ "0" ]]; }; do
    echo "Workers are still busy or queue is not empty..."
    echo "Elapsed time is: ${SECONDS} seconds"
    sleep 5

    QUEUE_SIZE=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${QUEUE_SIZE_QUERY}" )" | jq .data.result[0].value[1])
    WORKER_BUSY_THREADS_COUNT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${WORKER_BUSY_THREADS_QUERY}" )" | jq .data.result[0].value[1])

done

echo "Jobs finished in $SECONDS seconds."

# delete chaos files
echo "Deleting chaos objects..."

kubectl delete -f "$(pwd)/chaos-files"

# variables used in the queries
RANGE=${SECONDS}s
echo "Range is ${RANGE} seconds"
INTERVAL=15s

### Prometheus dependability metric query variables ###
BACKEND_AVAILABILITY_QUERY="avg_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}])"

BACKEND_UNAVAILABILITY_QUERY="(1 - sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${INTERVAL}])) / count_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${INTERVAL}])"

BACKEND_MUT_QUERY="15 * sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) / (floor((changes(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) + 1 + probe_success{instance=~'.*kubedepend-backend.*'} offset ${RANGE}) / 2))"

BACKEND_MDT_QUERY="15 * (count_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) - sum_over_time(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}])) / (floor((changes(probe_success{instance=~'.*kubedepend-backend.*'}[${RANGE}]) + 2 - probe_success{instance=~'.*kubedepend-backend.*'} offset ${RANGE}) / 2))"

BACKEND_MTBF_QUERY="${BACKEND_MUT_QUERY} + ${BACKEND_MDT_QUERY}"


### GET dependability metrics ###
printf "RESULTS\n"

BACKEND_AVAILABILITY=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${BACKEND_AVAILABILITY_QUERY}" )" | jq .data.result[0].value[1])
echo "Backend availability: ${BACKEND_AVAILABILITY}"
BACKEND_MUT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${BACKEND_MUT_QUERY}" )" | jq .data.result[0].value[1])
echo "Backend MUT: ${BACKEND_MUT}"
BACKEND_MDT=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${BACKEND_MDT_QUERY}" )" | jq .data.result[0].value[1])
echo "Backend MDT: ${BACKEND_MDT}"
BACKEND_MTBF=$(curl --silent "${PROMETHEUS_QUERY_URL}$( urlencode "${BACKEND_MTBF_QUERY}" )" | jq .data.result[0].value[1])
echo "Backend MDT: ${BACKEND_MTBF}"


### Write results in file
# Create folder
TIMESTAMP="$( date +"%d-%m-%Y_%H:%M:%S" )"
RESULTS_FOLDER=./results/test-results-"${TIMESTAMP}"

mkdir "${RESULTS_FOLDER}"


RESULTS_FILE="${RESULTS_FOLDER}/results.txt"
touch "${RESULTS_FILE}"

{ 
    printf "%s:\t%s\n" "Test time" "${SECONDS} seconds";
    printf "%s:\t%s\n" "Backend availability" "${BACKEND_AVAILABILITY}";
    printf "%s:\t%s\n" "Backend MUT" "${BACKEND_MUT}";
    printf "%s:\t%s\n" "Backend MDT" "${BACKEND_MDT}";
    printf "%s:\t%s\n" "Backend MTBF" "${BACKEND_MTBF}";  } >> "${RESULTS_FILE}"

# Save used inputs next to results
cp -r "$(pwd)/chaos-files" "${RESULTS_FOLDER}"