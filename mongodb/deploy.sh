#!/bin/bash -e

CWD=$(dirname $0)

kubectl apply --namespace sandbox -f "$CWD/deployment.yaml"
