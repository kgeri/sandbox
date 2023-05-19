#!/bin/bash

PWD=$(dirname "$0")

"$PWD"/gradlew installDist
"$PWD"/build/install/micronaut-demo/bin/micronaut-demo
