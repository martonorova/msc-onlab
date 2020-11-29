#!/bin/bash

# Open backend port on localhost
# currently by hand

# apply chaos files
echo "Creating chaos objects..."

kubectl apply -f "$(pwd)/chaos-files"

echo "Chaos objects created."

echo "Testing in progress..."
SECONDS=0

artillery run ../artillery/submit_jobs.yaml

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