#!/bin/bash
# This destroys the cluster on DigitalOcean

CLUSTER_NAME=sandbox

echo "Deleting cluster $CLUSTER_NAME..."
doctl k8s cluster delete $CLUSTER_NAME
