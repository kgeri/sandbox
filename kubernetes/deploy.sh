#!/bin/bash

./gradlew dockerBuildNative

docker tag kubernetes registry.digitalocean.com/ogreg/hello
docker push registry.digitalocean.com/ogreg/hello

kubectl apply -f config/hello-deployment.yaml
