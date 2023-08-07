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


function checkEnv {
    if ! command -v mvn &> /dev/null
    then
      echo "mvn could not be found. Please make sure mvn executable is in your ${PATH}."
      exit 1
    fi

    JAVA_VERSION_MIN=11
    CURRENT_JAVA_VERSION=`mvn -v | grep 'Java version' | awk '{split($3, a, "."); print a[1]}'`

    if [[ "${CURRENT_JAVA_VERSION}" -lt "${JAVA_VERSION_MIN}" ]]
    then
        echo "Java version must be >=$JAVA_VERSION_MIN. Please set \$JAVA_HOME to point to a JDK installation of version >= 11"
        exit 1
    fi
}


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DISTRIBUTION_DIR="${SCRIPT_DIR}/distribution"
FTP_SRC="ftp://ftp.ebi.ac.uk/pub/contrib/UniProt/UniFIRE/rules"

checkEnv
pushd ${SCRIPT_DIR} > /dev/null

echo "Building UniFIRE and downloading dependencies..."
mvn clean install -Dmaven.test.skip=true -Dmaven.source.skip=true
cd ${DISTRIBUTION_DIR}
mvn dependency:copy-dependencies
echo "Done building UniFIRE and downloading dependencies."

popd > /dev/null

unirule_version="$(grep -E "^URML_RULES_VERSION=" "${SCRIPT_DIR}"/docker/versions.properties | cut -d '=' -f 2)"

echo "Downloading rule urml files..."
for file_prefix in arba-urml unirule-urml unirule-templates unirule.pirsr-urml;
do
    wget ${FTP_SRC}/${file_prefix}-${unirule_version}.xml -O ${SCRIPT_DIR}/samples/${file_prefix}-latest.xml
done
echo "Done downloading rule urml files."

PIRSR_DATA_SRC="https://proteininformationresource.org/pirsr/pirsr_data_latest.tar.gz"
echo "Download pirsr data files..."
wget -q ${PIRSR_DATA_SRC} -O ${SCRIPT_DIR}/samples/pirsr_data_latest.tar.gz
echo "untarring pirsr_data..."
tar -zxf ${SCRIPT_DIR}/samples/pirsr_data_latest.tar.gz -C ${SCRIPT_DIR}/samples/
echo "Done download pirsr data files."