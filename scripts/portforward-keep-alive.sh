#!/bin/bash

# keep kubectl port-forward connection alive

if [[ -n $1  ]]
then
    port=$1

    while true;
    do
        curl -Ss "localhost:$port" > /dev/null ; sleep 10 ;
    done
else
    echo "Error: some parameters are empty"
    exit 1
fi