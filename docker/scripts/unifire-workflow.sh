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

UNIFIRE_REPO="/opt/git/unifire"
INTERPROSCAN_REPO="/opt/interproscan"
ETE4FOLDER="/opt/ete4"
VERSION_PROP_FILE="/opt/scripts/bin/versions.properties"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="${JAVA_HOME}/bin:${PATH}"
VOLUME=/volume

# user can either provide a fasta file or iprscan xml input file.
# If iprscan xml file is provided, then iprscan_flag is set to false and interproscan is skipped

#expected fasta filename (if provided)
fastafile=${VOLUME}/proteins.fasta

#expected iprscan filename (if provided)
iprscanfile=${VOLUME}/proteins-ipr.xml

# Initialize the flag
iprscan_flag=true
infile=""
# Check for files and set the flag
if [ -f "${iprscanfile}" ]; then
    iprscan_flag=false
    infile=${iprscanfile}
elif [ -f "${fastafile}" ]; then
    iprscan_flag=true
    infile=${fastafile}
else
    echo "No relevant input files (fasta or iprscan) found in the directory."
    exit 1
fi

iprscan_lineage_file=${VOLUME}/proteins_lineage-ipr.xml

function check_iprscan_version() {
    input_iprscan_version="$(head -n 5 ${iprscanfile} | grep -o 'interproscan-version="[^"]*"' | sed -E 's/interproscan-version="([^"]*)"/\1/')"
    unifire_iprscan_version="$(grep -E "^INTERPROSCAN_VERSION=" "${VERSION_PROP_FILE}" | cut -d '=' -f 2)"
    if [[ "${input_iprscan_version}" != "${unifire_iprscan_version}" ]]; then
        # If the versions do not match, show warning to the user (colored with ANSI escape codes)
        echo -e "\033[33mWARNING: Input interproscan version ${input_iprscan_version} does not match UniFIRE interproscan version ${unifire_iprscan_version} used for the rules." \
            "This may cause some compatibility issues. It is recommended to run interproscan with the same version as UniFIRE.\033[0m"
    fi
}

# Conditionally run or skip interproscan
if $iprscan_flag; then
    echo "Running interproscan on input fasta file ${fastafile}..."
    ${INTERPROSCAN_REPO}/interproscan.sh -f xml -dp -i ${fastafile} \
        --appl "Hamap,ProSiteProfiles,ProSitePatterns,Pfam,NCBIFAM,SMART,PRINTS,SFLD,CDD,Gene3D,PIRSF,PANTHER,SUPERFAMILY,FunFam" \
        -o ${iprscanfile}
else
    echo "Skipping interproscan and using provided file ${iprscanfile}..."
    check_iprscan_version
fi

echo "Updating taxonomy lineage for interproscan file ${iprscanfile} into ${iprscan_lineage_file}..."
${UNIFIRE_REPO}/misc/taxonomy/updateIPRScanWithTaxonomicLineage.py -i ${iprscanfile} -o ${iprscan_lineage_file} \
  -t ${ETE4FOLDER}/taxa.sqlite

echo "Running PIRSR hmmalign..."
${UNIFIRE_REPO}/distribution/bin/pirsr.sh -i ${iprscan_lineage_file} \
    -o ${VOLUME} -a /usr/bin/hmmalign -d ${UNIFIRE_REPO}/samples/pirsr_data

echo "Running rules inference on UniRule..."
${UNIFIRE_REPO}/distribution/bin/unifire.sh -r ${UNIFIRE_REPO}/samples/unirule-urml-latest.xml \
    -i ${iprscan_lineage_file} -t  ${UNIFIRE_REPO}/samples/unirule-templates-latest.xml \
    -o ${VOLUME}/predictions_unirule.out

echo "Running rules inference on ARBA..."
${UNIFIRE_REPO}/distribution/bin/unifire.sh -n 500 -r ${UNIFIRE_REPO}/samples/arba-urml-latest.xml \
    -i ${iprscan_lineage_file} \
    -o ${VOLUME}/predictions_arba.out

echo "Running rules inference on PIRSR..."
${UNIFIRE_REPO}/distribution/bin/unifire.sh -r ${UNIFIRE_REPO}/samples/unirule.pirsr-urml-latest.xml \
    -i ${VOLUME}/proteins_lineage-ipr-urml.xml -s XML -t  ${UNIFIRE_REPO}/samples/pirsr_data/PIRSR_templates.xml \
    -o ${VOLUME}/predictions_unirule-pirsr.out

# prediction output files must belong to the same user and group as input file
ownership=`stat -c "%u:%g" ${infile}`
for outfile in  proteins_lineage-ipr.xml proteins_lineage-ipr-urml.xml predictions_unirule.out \
  predictions_arba.out predictions_unirule-pirsr.out seq aln
do
  if [[ -e ${VOLUME}/${outfile} ]]
  then
    chown -R ${ownership} ${VOLUME}/${outfile}
  fi
done

echo "Finished running UniFIRE workflow..."

