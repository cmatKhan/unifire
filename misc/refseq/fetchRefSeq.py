#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
The fetchRefSeq.py script take a one / a list of RefSeq ID(s) as input and fetches data from the UniProt Protein API.
The generated output is a valid UniFIRE input file in Fact XML format.
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

import requests, json, sys, re, io, math
from lxml.etree import Element, SubElement, Comment, ElementTree, fromstring, XMLParser
import os.path

__copyright__ = "Copyright 2018, European Molecular Biology Laboratory"
__license__ = "Apache 2.0"
__maintainer__ = "EMBL-EBI - Protein Function Development Team"
__status__ = "Prototype"
__author__ = "Alexandre Renaux"

taxonomy_id_endpoint = "https://www.ebi.ac.uk/proteins/api/taxonomy/id/"
taxonomy_lineage_endpoint = "https://www.ebi.ac.uk/proteins/api/taxonomy/lineage/"
uniparc_db_ref_endpoint = "https://www.ebi.ac.uk/proteins/api/uniparc/dbreference/"
uniparc_upi_endpoint = "https://www.ebi.ac.uk/proteins/api/uniparc/upi/"

refseq_id_pattern = re.compile('([A-Z]P_\d+)(.\d*)?.*')
refseq_id_format_uf = "[A-Z]P_[0-9]+(.[0-9]+)?"

taxId_to_data = {}


def usage():
    print("Usage:")
    print("./refseq-convertor.py <RefSeq_ID/RefSeq_list_file> <output_path>")
    print("<RefSeq_ID/RefSeq_list_file>: Either a RefSeq ID or a file containing a list of them")
    print("      <> RefSeq_ID: A RefSeq protein ID. Format: "+refseq_id_format_uf)
    print("      <> RefSeq_list_file: Path of a text file with a list of them (1 per line)")
    print("<output_path>: Path of an XML file to write the output UniParc XML")
    sys.exit(1)


class TaxonomyData:
    def __init__(self, lineage_ids, scientific_name):
        self.lineage_ids = lineage_ids
        self.scientific_name = scientific_name


def get_taxonomy_data(tax_id):
    if tax_id in taxId_to_data:
        return taxId_to_data[tax_id]
    else:
        lineage_request_url = taxonomy_lineage_endpoint + str(tax_id)
        r_lineage = requests.get(lineage_request_url, headers={"Accept": "application/json"})
        id_request_url = taxonomy_id_endpoint + str(tax_id)
        r_id = requests.get(id_request_url, headers={"Accept": "application/json"})
        if r_lineage.ok and r_id.ok:
            lineage = json.loads(r_lineage.text)["taxonomies"]
            scientific_name = json.loads(r_id.text)["scientificName"]
            lineage_ids = [taxon['taxonomyId'] for taxon in reversed(lineage)]
            taxonomy_data = TaxonomyData(lineage_ids, scientific_name)
            taxId_to_data[tax_id] = taxonomy_data
            return taxonomy_data


def get_uniparc_entry_xml_from_uniparc_id(upi, refseq_id):
    request_url = uniparc_upi_endpoint + str(upi)
    r = requests.get(request_url, headers={"Accept": "application/xml"})
    if r.ok:
        parser = XMLParser(ns_clean=True, recover=True, encoding='utf-8')
        xml_tree = fromstring(r.text.encode('utf-8'), parser=parser)
        root = xml_tree.findall('.//')
        db_ref_counter = 0
        ncbi_tax_id_counter = 0
        for element in root:
            if element.tag == "{http://uniprot.org/uniparc}dbReference":
                if element.attrib["type"] == "RefSeq" and element.attrib["id"] == refseq_id and element.attrib["active"] == "Y":
                    for propertyElement in element.iter("{http://uniprot.org/uniparc}property"):
                        if propertyElement.attrib["type"] == "NCBI_taxonomy_id":
                            tax_id = propertyElement.attrib["value"]
                            taxonomy_data = get_taxonomy_data(tax_id)
                            lineage_property = SubElement(element, "property")
                            lineage_property.set("type", "NCBI_taxonomy_lineage_ids")
                            lineage_property.set("value", ",".join([str(tax_id) for tax_id in taxonomy_data.lineage_ids]))
                            scientific_name_property = SubElement(element, "property")
                            scientific_name_property.set("type", "organism_scientific_name")
                            scientific_name_property.set("value", taxonomy_data.scientific_name)
                            ncbi_tax_id_counter += 1
                    db_ref_counter += 1
                else:
                    xml_tree.remove(element)
        if db_ref_counter > 1:
            return None, "Warning: Multiple RefSeq dbReferences for " + upi + " (" + refseq_id + ")"
        if db_ref_counter == 0:
            return None, "Warning: Missing RefSeq dbReference for "+upi+" ("+refseq_id+")"
        if ncbi_tax_id_counter == 0:
            return None, "Warning: Missing NCBI Taxonomy Id for "+upi+" ("+refseq_id+")"
        return xml_tree, None


def get_uniparc_entry_xml_from_uniparc_ids(uniparc_ids, refseq_id):
    valid_uniparc_entries = []
    errors = []
    for upi in uniparc_ids:
        (response, error) = get_uniparc_entry_xml_from_uniparc_id(upi, refseq_id)
        if error is None:
            valid_uniparc_entries.append(response)
        else:
            errors.append(error)
    if len(valid_uniparc_entries) == 0:
        print("Warning: No corresponding UniParc entry for RefSeq "+refseq_id)
        for error in errors:
            print("  |_ "+error)
        return None
    else:
        selected_valid_uniparc_entry = valid_uniparc_entries[0]
        if len(valid_uniparc_entries) > 1:
            print("Warning: Multiple valid UniParc entries for RefSeq "+refseq_id+". Resolved with "+selected_valid_uniparc_entry)
        return selected_valid_uniparc_entry


def get_uniparc_entry_xml_from_refseq_id(refseq_id):
    request_url = uniparc_db_ref_endpoint + str(refseq_id)
    r = requests.get(request_url, headers={"Accept": "application/json"})
    if r.ok:
        uniparc_response = json.loads(r.text)
        if len(uniparc_response) >= 1:
            uniparc_ids = [entry["accession"] for entry in uniparc_response]
            return get_uniparc_entry_xml_from_uniparc_ids(uniparc_ids, refseq_id)
        elif len(uniparc_response) == 0:
            print("Warning: No corresponding UniParc for refseq id:" + str(refseq_id))
    return None


def check_and_format_refseq_id(refseq_id):
    if not refseq_id:
        return None
    if re.match(refseq_id_pattern, refseq_id):
        return re.sub(refseq_id_pattern, "\g<1>", refseq_id)
    else:
        print("Warning: Unexpected ID: \""+refseq_id+"\". Expected format: "+refseq_id_format_uf+" --> Skipped.")
        return None


def write_uniparc_xml_from_refseq_id_list(refseq_id_list, file_out):
    with open(file_out, 'wb') as f_out:
        f_out.write(b"""<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<uniparc xmlns=\"http://uniprot.org/uniparc\">
""")
        refseq_ids_total = len(refseq_id_list)
        percentage_progress_step = 10
        percentage_progress = 0
        counter = 0
        for refseq_id in refseq_id_list:
            formatted_refseq_id = check_and_format_refseq_id(refseq_id)
            if formatted_refseq_id is not None:
                entry_xml = get_uniparc_entry_xml_from_refseq_id(formatted_refseq_id)
                if entry_xml is None:
                    print("Warning: "+refseq_id+" will be skipped.")
                    continue
                ElementTree(entry_xml).write(f_out, encoding='utf-8',  method='xml',  pretty_print=True)
                counter += 1
                percentage = (float(counter)/refseq_ids_total)*100
                if percentage >= percentage_progress:
                    print("UniParc fetching progress ............ "+str(math.floor(percentage))+"%")
                    percentage_progress += percentage_progress_step
        f_out.write(b"""
</uniparc>
""")


def get_refseq_id_list_from_file(file_refseq_ids):
    refseq_id_list = []
    with io.open(file_refseq_ids, 'r', encoding='utf-8') as f_in:
        for refseq_id in f_in:
            refseq_id_list.append(refseq_id.strip())

        return refseq_id_list


def main(argv):
    if len(argv) < 2:
        usage()
        sys.exit(1)
    try:
        refseq_id_or_file = argv[1]
        if os.path.isfile(refseq_id_or_file):
            refseq_id_list = get_refseq_id_list_from_file(refseq_id_or_file)
        else:
            refseq_id_list = [refseq_id_or_file]
        file_out = argv[2]
    except Exception as e:
        usage()
        raise e

    write_uniparc_xml_from_refseq_id_list(refseq_id_list, file_out)


if __name__ == "__main__":
    main(sys.argv)
