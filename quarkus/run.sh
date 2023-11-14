#!/bin/bash

PWD=$(dirname "$0")

"$PWD"/gradlew quarkusBuild
java -jar "$PWD"/build/quarkus-app/quarkus-run.jar
