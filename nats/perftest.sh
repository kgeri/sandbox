#!/bin/bash

export TEST_NAME=$1
if [ -z "$TEST_NAME" ]; then
    echo "Usage: $0 <test_name>"
    exit 1
fi

./gradlew jibDockerBuild
docker push srvu:5000/nats-perftest
kubectl delete job --namespace sandbox nats-perftest
envsubst < job.nats-perftest.yaml | kubectl apply --namespace sandbox -f -
