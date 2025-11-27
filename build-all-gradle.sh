#!/bin/bash

cwd=$(dirname $0)

for gradlew_path in "$cwd"/*/gradlew; do
    project_dir=$(dirname "$gradlew_path")
    build_log=/tmp/$(basename "$project_dir").log

    echo -n "Building $project_dir..." >&2
    (cd "$project_dir" && ./gradlew build > "$build_log" 2>&1)

    if [ $? -ne 0 ]; then
        echo -e "\033[31mFAILED (see $build_log)\033[0m" >&2
    else
        echo -e "\033[32mOK\033[0m" >&2
    fi
done
