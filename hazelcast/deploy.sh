#!/bin/bash

./gradlew jibDockerBuild
docker push srvu:5000/hazelcast-sandbox
kubectl apply -f deployment.yaml
