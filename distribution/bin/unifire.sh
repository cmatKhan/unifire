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

set -e
set -u

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
JAVA_VERSION_MIN=17
SCRIPT_DIR=$(get_script_dir)

# Default values
# default min heap size 4G
MIN_HEAP_MEM_OPTION="-Xms4G"
# default max heap size 75% of available RAM
MAX_HEAP_MEM_OPTION="-XX:MaxRAMPercentage=75"
function run {
    local cmdArgs="${@}"
    local memory
    memory=$(echo $cmdArgs | grep -P "\-m(\s+)?\d+" | sed -E 's/.*-m *([0-9]+).*/\1/g')
    #if heap memory provided with -m arg, then it is used instead of the percentage option
    if [[ -n "${memory}" ]]
    then
        MAX_HEAP_MEM_OPTION="-Xmx${memory}M"
    fi
    java --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -XshowSettings:vm "${MIN_HEAP_MEM_OPTION}" "${MAX_HEAP_MEM_OPTION}" -cp "${SCRIPT_DIR}/../target/*:${SCRIPT_DIR}/../target/dependency/*" uk.ac.ebi.uniprot.unifire.UniFireApp ${cmdArgs}
}

function checkEnv {
    local CURRENT_JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {split($2,a,"."); print a[1]}')
    if [[ "${CURRENT_JAVA_VERSION}" -lt "${JAVA_VERSION_MIN}" ]]
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
