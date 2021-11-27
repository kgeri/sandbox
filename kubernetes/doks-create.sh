#!/bin/bash
# This creates a simple 2-node Kubernetes cluster on DigitalOcean
# Pre-requisites:
# - doctl properly set up (see the DO guide)
# - an API key created and configured in doctl

CLUSTER_NAME=sandbox

# Creating the cluster
if ! doctl k8s cluster list | grep -q $CLUSTER_NAME; then
  echo "Creating cluster $CLUSTER_NAME, grab a coffee..."
  doctl k8s cluster create $CLUSTER_NAME \
    --count 2 \
    --size=s-1vcpu-2gb \
    --region=fra1

  # Configuring the registry
  doctl registry kubernetes-manifest | kubectl apply -f -
fi
