#!/bin/bash

PWD=$(dirname "$0")

"$PWD"/gradlew quarkusBuild -Dquarkus.package.type=native
"$PWD"/build/quarkus-demo-1.0.0-SNAPSHOT-runner
