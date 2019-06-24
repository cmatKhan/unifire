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

function backup_file {
    filename=$1
    if [[ -e ${filename} ]]; then
        mv ${filename} ${filename}_
    fi
}


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DISTRIBUTION_DIR="${SCRIPT_DIR}/distribution"
FTP_SRC="http://ftp.ebi.ac.uk/pub/contrib/UniProtKB/UniFIRE/rules/"

pushd ${SCRIPT_DIR} > /dev/null

echo "Building UniFIRE and downloading dependencies..."
mvn clean install -Dmaven.test.skip=true -Dmaven.source.skip=true
cd ${DISTRIBUTION_DIR}
mvn dependency:copy-dependencies
echo "Done building UniFIRE and downloading dependencies."

popd > /dev/null


echo "Downloading rule urml files..."
for i in aa-rules-saas-urml-latest.xml aa-rules-unirule-urml-latest.xml unirule-templates-latest.xml;
do
    backup_file ${SCRIPT_DIR}/samples/${i}
    wget ${FTP_SRC}/${i} -O ${SCRIPT_DIR}/samples/${i}
done
echo "Done downloading rule urml files."


