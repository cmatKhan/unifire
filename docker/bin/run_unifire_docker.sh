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

function usage() {
    echo "usage: $0 -i <INPUT_FILE> -i <OUTPUT_FOLDER> [-v <VERSION> [-w <WORKING_FOLDER [-c]]]"
    echo "    -i: Path to multi-FASTA input file with headers in UniProt FASTA header format, containing at least"
    echo "        OX=<taxid>. (Required)"
    echo "    -o: Path to output folder. All output files with predictions in TSV format will be available in this"
    echo "        folder at the end of the procedure. (Required)"
    echo "    -v: Version of the docker image to use, e.g. 2020.2. Available versions are listed under"
    echo "        https://gitlab.ebi.ac.uk/uniprot-public/unifire/container_registry. (Optional), DEFAULT: 2020.3"
    echo "    -w: Path to an empty working directory.  If this option is not given, then a temporary folder will be"
    echo "        created and used to store intermediate files. (Optional)"
    echo "    -c: Clean up temporary files. If set, then all temporary files will be cleaned up at the end of the"
    echo "        procedure. If no working directory is provided through option -w then the temporary files are cleaned"
    echo "        up by default"
    exit 1
}

infile=""
outdir=""
workdir=""
cleanworkdir=0
docker_version="2020.3"
predictionfiles="predictions_unirule.out predictions_saas.out predictions_unirule-pirsr.out"

while getopts "i:o:w:c:v:" optionName
do
  case "${optionName}" in
    i) infile=${OPTARG};;
    o) outdir=${OPTARG};;
    w) workdir=${OPTARG};;
    v) docker_version=${OPTARG};;
    c) cleanworkdir=1;;
  esac
done

# infile
function check_infile() {
    if [[ ! -f ${infile} ]]
    then
      echo "Error: Input file ${infile} not found!"
      usage
    fi
}

# outdir
function check_outdir() {
    if [[ ! -d ${outdir} ]]
    then
      echo "Given outdir ${outdir} does not exist. Trying to create it..."
      set +e
      mkdir -p ${outdir}
       if [[ $? == 0 ]]
      then
        echo "Successfullt created output directory ${outdir}."
      else
        echo "Failed to create output directory ${outdir}."
        usage
      fi
      set -e
    fi
}

# workdir
function check_workdir() {
    usemktmp=0
    if [[ ${workdir} == "" ]]
    then
      usemktmp=1
      echo "No working directory given. Creating temporary directory."
    elif  [[ ! -d ${workdir} ]]
    then
      echo "Given working directory does not exist. Trying to create it ..."
      set +e
      mkdir -p ${workdir}
      if [[ $? == 0 ]]
      then
        echo "Successfullt created working directory ${workdir}"
      else
        usemktmp=1
        echo "Failed to create working directory ${workdir}. Creating temporary directory."
      fi
      set -e
    fi
    
    if [[ ${usemktmp} == 0 ]] && [[ ! -z "$(ls -A ${workdir})" ]]
    then
      usemktmp=1
      echo "Given working directory ${workdir} is not empty. Creating temporary directory instead."
    fi
    
    if [[ ${usemktmp} == 1 ]]
    then
      workdir=`mktemp -d`
      cleanworkdir=1
      echo "Using ${workdir} for temporary files. Please make sure there is enough free space on the according filesystem."
    fi
}

# Run the docker image on $the prepared {workdir}
function run_docker_image() {
    cp ${infile} ${workdir}/proteins.fasta
    docker run \
        --mount type=bind,source=${workdir},target=/volume \
        dockerhub.ebi.ac.uk/uniprot-public/unifire:${docker_version}
}

# Move output files from ${workdir} to ${outdir}
function move_output_files() {
    for predictionfile in ${predictionfiles}
    do
      echo Copying prediction file ${predictionfile} to ${outdir}
      cp -p ${workdir}/${predictionfile} ${outdir}/
    done
}

# Clean up
function cleanup_workdir() {
    if [[ ${cleanworkdir} == 1 ]]
    then
      echo "Cleaning up folder ${workdir}"
      for predictionfile in ${predictionfiles}
      do
        rm -f ${workdir}/${predictionfile}
      done
      rm -f ${workdir}/proteins.fasta
      rm -f ${workdir}/proteins_lineage.fasta
      rm -f ${workdir}/proteins_lineage-ipr-urml.xml
      rm -f ${workdir}/proteins_lineage-ipr.xml
      rm -f ${workdir}/seq/*.fasta
      rm -f ${workdir}/aln/*.aln
      if [[ -d ${workdir}/aln ]];
      then
        rmdir ${workdir}/aln
      fi
      if [[ -d ${workdir}/seq ]]
      then
        rmdir ${workdir}/seq
      fi
    fi
}

# main
check_infile
check_outdir
check_workdir
run_docker_image
move_output_files
cleanup_workdir
