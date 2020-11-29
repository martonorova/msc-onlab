#!/bin/bash

# Open backend port on localhost
# currently by hand

# apply chaos files
echo "Creating chaos objects..."

kubectl apply -f "$(pwd)/chaos-files"

echo "Chaos objects created."

echo "Testing in progress..."

artillery run ../artillery/submit_jobs.yaml

# sleep 2

# delete chaod files
echo "Deleting chaos objects..."

kubectl delete -f "$(pwd)/chaos-files"

echo "Chaos objects deleted."