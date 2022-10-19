#!/bin/bash -e

CWD=$(dirname $0)

# Docker cleanup
docker container prune -f
docker image prune -f

# Build
"$CWD"/gradlew jibDockerBuild

# Deploy
IMAGE_ID=$(cut -c 8- "$CWD/build/jib-image.id")
export IMAGE_TAG=$(cut -c 8-16 "$CWD/build/jib-image.digest")
docker tag "$IMAGE_ID" "srvu:5000/mongo-server:$IMAGE_TAG"
docker push "srvu:5000/mongo-server:$IMAGE_TAG"

envsubst < "$CWD/deployment-mongo.yaml" | kubectl apply --namespace sandbox -f -
envsubst < "$CWD/deployment-mongo-server.yaml" | kubectl apply --namespace sandbox -f -
