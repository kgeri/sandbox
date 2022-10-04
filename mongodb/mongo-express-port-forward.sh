#!/bin/bash

kubectl port-forward --namespace sandbox service/mongo 8081:8081
