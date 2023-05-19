#!/bin/bash

PWD=$(dirname "$0")

"$PWD"/gradlew nativeCompile
"$PWD"/build/native/nativeCompile/boot-demo
