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

ROOT_FOLDER="/opt"
DOWNLOAD_FOLDER="/opt/download"

mkdir -p ${DOWNLOAD_FOLDER}
cd ${DOWNLOAD_FOLDER}
echo "Downloading InterProScan..."
wget -q ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/5.45-80.0/interproscan-5.45-80.0-64-bit.tar.gz
echo "Done."
wget -q ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/5.45-80.0/interproscan-5.45-80.0-64-bit.tar.gz.md5

ipr_check=`md5sum -c interproscan-5.45-80.0-64-bit.tar.gz.md5`

if [[ ${ipr_check} != "interproscan-5.45-80.0-64-bit.tar.gz: OK" ]]
then
	exit 11
fi

mkdir -p ${ROOT_FOLDER}
cd ${ROOT_FOLDER}
echo "Extracting InterProScan..."
tar -pxzf ${DOWNLOAD_FOLDER}/interproscan-5.45-80.0-64-bit.tar.gz
echo "Done."

cd ${DOWNLOAD_FOLDER}
echo "Downloading Panther data..."
wget -q ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/data/panther-data-14.1.tar.gz
echo "Done."
wget -q ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/data/panther-data-14.1.tar.gz.md5

panther_check=`md5sum -c panther-data-14.1.tar.gz.md5`
if [[ ${panther_check} != "panther-data-14.1.tar.gz: OK" ]]
then
	exit 12
fi

cd ${ROOT_FOLDER}/interproscan-5.45-80.0/data
echo "Extracting Panther data..."
tar -pxzf ${DOWNLOAD_FOLDER}/panther-data-14.1.tar.gz
echo "Done."

# Clean up tar to reduce the size of the image
rm -f ${DOWNLOAD_FOLDER}/interproscan-5.45-80.0-64-bit.tar.gz
rm -f ${DOWNLOAD_FOLDER}/interproscan-5.45-80.0-64-bit.tar.gz.md5
rm -f ${DOWNLOAD_FOLDER}/panther-data-14.1.tar.gz
rm -f ${DOWNLOAD_FOLDER}/panther-data-14.1.tar.gz.md5
