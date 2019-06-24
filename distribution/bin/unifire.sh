#!/usr/bin/env bash

############################################################################
#    Copyright (c) 2018 European Molecular Biology Laboratory
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
############################################################################

get_script_dir () {
     SOURCE="${BASH_SOURCE[0]}"

     while [ -h "$SOURCE" ]; do
          DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
          SOURCE="$( readlink "$SOURCE" )"

          [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
     done

     DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
     echo "$DIR"
}

# Environment
JAVA_VERSION_MIN=1.8
SCRIPT_DIR=$(get_script_dir)

# Default values
DEFAULT_MAX_MEMORY=4096

function run {
    local cmdArgs="${@}"
    local memory=$(echo $cmdArgs | grep -P "\-m(\s+)?\d+" | sed -E 's/.*-m *([0-9]+).*/\1/g')
    java -Xmx"${memory:-$DEFAULT_MAX_MEMORY}M" -cp "${SCRIPT_DIR}/../target/*:${SCRIPT_DIR}/../target/dependency/*" uk.ac.ebi.uniprot.unifire.UniFireApp ${cmdArgs}
}

function checkEnv {
    local VALID_JAVA_VERSION=$(java -version 2>&1 | head -n 1 | sed 's/^.*"\([0-9]\.[0-9]\)\..*"$/\1/g' | awk -v v=${JAVA_VERSION_MIN} '{print ($1 >= v)}')
    if [ ${VALID_JAVA_VERSION} -eq 0 ]
    then
        >&2 echo "Java version must be >=$JAVA_VERSION_MIN."
        exit 1
    fi
}

function main {
    checkEnv
    run "${@}"
}

main "${@}"
