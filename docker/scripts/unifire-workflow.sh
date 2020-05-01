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

UNIFIRE_REPO="/opt/git/unifire"
INTERPROSCAN_REPO="/opt/interproscan-5.44-79.0"

VOLUME=/volume
infilename=infile.fasta

cd ${UNIFIRE_REPO}
./misc/taxonomy/fetchLineageLocal.py ${VOLUME}/proteins.fasta ${VOLUME}/proteins_lineage.fasta

${INTERPROSCAN_REPO}/interproscan.sh -f xml -dp -i ${VOLUME}/proteins_lineage.fasta \
    --appl "Hamap,ProSiteProfiles,ProSitePatterns,Pfam,TIGRFAM,SMART,PRINTS,SFLD,CDD,Gene3D,ProDom,PIRSF,PANTHER,SUPERFAMILY" \
    -o ${VOLUME}/proteins_lineage-ipr.xml

PATH="/usr/lib/jvm/java-8-openjdk-amd64/bin:${PATH}"
${UNIFIRE_REPO}/distribution/bin/pirsr.sh -i ${VOLUME}/proteins_lineage-ipr.xml \
    -o ${VOLUME} -a /usr/bin/hmmalign -d ${UNIFIRE_REPO}/samples/pirsr_data

${UNIFIRE_REPO}/distribution/bin/unifire.sh -r ${UNIFIRE_REPO}/samples/unirule-urml-latest.xml \
    -i ${VOLUME}/proteins_lineage-ipr.xml -t  ${UNIFIRE_REPO}/samples/unirule-templates-latest.xml \
    -o ${VOLUME}/predictions_unirule.out

${UNIFIRE_REPO}/distribution/bin/unifire.sh -r ${UNIFIRE_REPO}/samples/saas-urml-latest.xml \
    -i ${VOLUME}/proteins_lineage-ipr.xml \
    -o ${VOLUME}/predictions_saas.out

${UNIFIRE_REPO}/distribution/bin/unifire.sh -n 100 -r ${UNIFIRE_REPO}/samples/unirule.pirsr-urml-latest.xml \
    -i ${VOLUME}/proteins_lineage-ipr-urml.xml -s XML -t  ${UNIFIRE_REPO}/samples/pirsr_data/PIRSR_templates.xml \
    -o ${VOLUME}/predictions_unirule-pirsr.out

# prediction output files must belong to the same user and group as proteins.fasta input file
ownership=`stat -c "%u:%g" ${VOLUME}/proteins.fasta`
for outfile in  proteins_lineage.fasta proteins_lineage-ipr.xml proteins_lineage-ipr-urml.xml predictions_unirule.out \
  predictions_saas.out predictions_unirule-pirsr.out seq aln
do
  chown -R ${ownership} ${VOLUME}/${outfile}
done


