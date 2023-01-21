#!/bin/bash -e

kubectl --namespace sandbox delete deployment nats-server || true
kubectl apply --namespace sandbox -f deployment.yaml
