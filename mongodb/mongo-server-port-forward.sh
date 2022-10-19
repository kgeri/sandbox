#!/bin/bash

kubectl port-forward --namespace sandbox service/mongo-server 8080:8080
