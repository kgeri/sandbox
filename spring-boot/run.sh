#!/bin/bash

PWD=$(dirname "$0")

"$PWD"/gradlew bootJar
java -jar "$PWD/build/libs/boot-demo-0.0.1-SNAPSHOT.jar"
