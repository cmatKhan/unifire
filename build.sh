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
FTP_SRC="ftp://ftp.ebi.ac.uk/pub/contrib/UniProt/UniFIRE/rules/"

pushd ${SCRIPT_DIR} > /dev/null

echo "Building UniFIRE and downloading dependencies..."
mvn clean install -Dmaven.test.skip=true -Dmaven.source.skip=true
cd ${DISTRIBUTION_DIR}
mvn dependency:copy-dependencies
echo "Done building UniFIRE and downloading dependencies."

popd > /dev/null


echo "Downloading rule urml files..."
for file in saas-urml-latest.xml unirule-urml-latest.xml unirule-templates-latest.xml unirule.pirsr-urml-latest.xml;
do
    backup_file ${SCRIPT_DIR}/samples/${file}
    wget ${FTP_SRC}/${file} -O ${SCRIPT_DIR}/samples/${file}
done
echo "Done downloading rule urml files."

PIRSR_DATA_SRC="https://proteininformationresource.org/pirsr/pirsr_data_latest.tar.gz"
echo "Download pirsr data files..."
backup_file ${SCRIPT_DIR}/samples/pirsr_data_latest.tar.gz
wget ${PIRSR_DATA_SRC} -O ${SCRIPT_DIR}/samples/pirsr_data_latest.tar.gz
tar zxvf ${SCRIPT_DIR}/samples/pirsr_data_latest.tar.gz -C ${SCRIPT_DIR}/samples/
echo "Done download pirsr data files."
