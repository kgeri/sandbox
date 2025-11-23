#!/bin/bash

PWD=$(dirname "$0")

# TODO: needed this override because Gradle can't find the proper JVM
export JAVA_HOME=$HOME/.sdkman/candidates/java/25-graalce

"$PWD"/gradlew nativeCompile
"$PWD"/build/native/nativeCompile/micronaut-demo
