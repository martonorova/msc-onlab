#!/bin/bash

if [[ -n $1 && -n $2 && -n $3 && -n $4 && -n $5 ]]
then
    namespace=$1
    servicename=$2
    hostport=$3
    targetport=$4
    path=$5

    # initial port forward
    echo "[PORT_FORWARD_KEEP_ALIVE] Start initial port-forwarding"
    kubectl -n "$namespace" port-forward service/"$servicename" "$hostport":"$targetport" &
    pw_pid=$!
    echo "[PORT_FORWARD_KEEP_ALIVE] Port-forwarding started"

    sleep 5

    # keep kubectl port-forward connection alive
    while true;
    do
        curl -Ss "localhost:$hostport$path" > /dev/null;

        # if curl failed, restart portforwarding
        if [ ! $? -eq 0 ]
        then
            echo "[PORT_FORWARD_KEEP_ALIVE] Could not connect to $servicename on port $targetport at path $path"
            kill -9 $pw_pid
            echo "[PORT_FORWARD_KEEP_ALIVE] Start new port-forwarding"
            kubectl -n "$namespace" port-forward service/"$servicename" "$hostport":"$targetport" &
            pw_pid=$!
            echo "[PORT_FORWARD_KEEP_ALIVE] Port-forwarding started"
        fi
        sleep 10;
    done
else
    echo "[PORT_FORWARD_KEEP_ALIVE] Error: some parameters are empty"
    exit 1
fi