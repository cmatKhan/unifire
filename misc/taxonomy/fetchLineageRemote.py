#!/usr/bin/python3
# -*- coding: utf-8 -*-

"""
The fetchLineageRemote script reads a MultiFasta file and will replace any occurence of "OX={taxId}" in the header
by the full lineage corresponding to this taxId. The output is the resolved multifasta file written on the given output path.

It is using the UniProt REST API [[https://www.ebi.ac.uk/proteins/api/doc/#!/taxonomy/getTaxonomyLineageById]].
Do NOT use this script when processing large amount of sequences from different species, to prevent hammering the service.

Library dependencies (via pip / conda / ...):
    * biopython
    * requests
    * json
"""
#  Copyright (c) 2018 European Molecular Biology Laboratory
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import requests, json, sys, re
from Bio import SeqIO

__copyright__ = "Copyright 2018, European Molecular Biology Laboratory"
__license__ = "Apache 2.0"
__maintainer__ = "EMBL-EBI - Protein Function Development Team"
__status__ = "Prototype"
__author__ = "Alexandre Renaux"

taxonomy_lineage_endpoint = "https://www.ebi.ac.uk/proteins/api/taxonomy/lineage/"
header_OX_pattern = re.compile('(OX=)(\d+)')
header_DE_remove_pattern = re.compile("([a-zA-Z0-9]+\|[a-zA-Z0-9]+\|[a-zA-Z0-9_]+)\s(.+?)(\s[A-Z]{2}=.+)")

taxId_to_lineage = {}

def usage():
    print("Usage:")
    print("./fetchLineageRemote.py <input> <output>")
    sys.exit(1)


def get_taxonomy_full_lineage(tax_id):
    if tax_id in taxId_to_lineage:
        return taxId_to_lineage[tax_id]
    else:
        request_url = taxonomy_lineage_endpoint + str(tax_id)
        r = requests.get(request_url, headers={ "Accept" : "application/json"})
        if r.ok:
            response_body = json.loads(r.text)
            lineage = response_body["taxonomies"]
            lineage_ids = [taxon['taxonomyId'] for taxon in reversed(lineage)]
            taxId_to_lineage[tax_id] = lineage_ids
            return lineage_ids


def resolve_header(header):
    tax_id_match = header_OX_pattern.search(header)
    if tax_id_match:
        tax_id = int(tax_id_match.group(2))
        lineage = get_taxonomy_full_lineage(tax_id)
        if lineage:
            replacement = "\g<1>" + ",".join(str(i) for i in lineage)
            return re.sub(header_OX_pattern, replacement, header)
    return header


def removeLongProteinName(description):
    match = header_DE_remove_pattern.search(description)
    if match:
        groups = list(header_DE_remove_pattern.search(description).groups())
        if len(groups[1]) > 127:
            del groups[1]
        return " ".join(groups)
    else:
        return description

def main(argv):
    if len(argv) < 2:
        usage()
    file_in = argv[1]
    file_out = argv[2]
    with open(file_out, 'w') as f_out:
        for seq_record in SeqIO.parse(open(file_in, mode='r'), "fasta"):
            seq_record.description = resolve_header(seq_record.description)
            seq_record.description = removeLongProteinName(seq_record.description)
            seq_record.id = ""
            r=SeqIO.write(seq_record, f_out, "fasta")
            if r!=1: print("Error while writing sequence:  " + seq_record.id)

if __name__ == "__main__":
    main(sys.argv)
