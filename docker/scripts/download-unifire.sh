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

GIT_REPO="/opt/git"

echo "Downloading and building UniFIRE..."
mkdir -p ${GIT_REPO}
cd ${GIT_REPO}
git clone https://gitlab.ebi.ac.uk/uniprot-public/unifire.git
cd unifire
git checkout TRM-31483-interproscan
./build.sh

echo "Done building UniFIRE."