/*
 *  Copyright (c) 2018 European Molecular Biology Laboratory
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.ac.ebi.uniprot.urml.input.parsers.fasta.header;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.OrganelleType;

/**
 * Parser to convert a fasta header into {@link FastaHeaderData}
 * The expected format is similar to the conventional UniProt fasta header
 * @see <a href="https://www.uniprot.org/help/fasta-headers">https://www.uniprot.org/help/fasta-headers</a>
 * This format has been sligshtly adjusted for UniFIRE and should follow this format:
 * >[db|]identifier|name [(Fragment)] OX=lineage [OS=organismName GN=geneName GL=olnOrOrf OG=organelles]
 * where:
 * <ul>
 *     <li>[db]: database name (opt.)</li>
 *     <li>identifier: a unique identifier for the protein</li>
 *     <li>name: the name of the protein. If unknown, a generic name like "unknown protein" is OK</li>
 *     <li>[(Fragment)]: add this flag if the protein is a fragment (opt.)</li>
 *     <li>lineage: the species lineage made of NCBI tax ids (comma separated list)</li>
 *     <li>[organismName]: scientific name of the organism (opt.)</li>
 *     <li>[geneName]: recommended gene name (if multiple (comma), first one will be kept) (opt.)</li>
 *     <li>[olnOrOrf]: recommended oln or orf (if multiple (comma), first one will be kept) (opt.)</li>
 *     <li>[organelles]: list of organelles (comma separated list, opt.). Must follow
 *     {@see <a href="https://www.ebi.ac.uk/ena/WebFeat/qualifiers/organelle.html">organelle ontology</a>}</li>
 * </ul>
 *
 * Any additional data will be simply ignored.
 *
 * @author Alexandre Renaux
 */
public class FastaHeaderParser {

    private static final Logger logger = LoggerFactory.getLogger(FastaHeaderParser.class);

    private static final String SECTION_DELIMITER = "\\|";
    private static final String MULTIVALUED_DELIMITER_REGEX = ",\\s?";
    private static final String ORGANISM_SCIENTIFIC_NAME_FLAG = "OS";
    private static final String ORGANISM_LINEAGE_FLAG = "OX";
    private static final String GENE_NAME_FLAG = "GN";
    private static final String GENE_OLN_OR_ORF_FLAG = "GL";
    private static final String GENE_ORGANELLE_LOCATION_FLAG = "OG";

    private static final Pattern fastaHeaderFlagsPattern = Pattern.compile("([A-Z]{2})=(.+?(?=[A-Z]{2}=)|.+$)");
    private static final Pattern fastaHeaderNamePattern = Pattern.compile("^(.+?)(\\(Fragment\\))?( [A-Z]{2}=?.*)?$");

    private final FastaHeaderDataValidator uniProtFastaHeaderDataValidator;

    public FastaHeaderParser() {
        this.uniProtFastaHeaderDataValidator = new FastaHeaderDataValidator();
    }

    public FastaHeaderData parse(String fastaHeader){
        FastaHeaderData uniProtFastaHeaderData = new FastaHeaderData(fastaHeader);

        if (fastaHeader.startsWith(">")){
            fastaHeader = fastaHeader.substring(1, fastaHeader.length());
        }
        String[] splittedHeader = fastaHeader.split(SECTION_DELIMITER);
        String metadata;

        if (splittedHeader.length == 3) {
            uniProtFastaHeaderData.setDatabase(splittedHeader[0]);
            uniProtFastaHeaderData.setIdentifier(splittedHeader[1]);
            metadata = splittedHeader[2];
        } else if (splittedHeader.length == 2){
            uniProtFastaHeaderData.setIdentifier(splittedHeader[0]);
            metadata = splittedHeader[1];
        } else {
            throw new FastaHeaderParseException(String.format(
                    "Malformed fasta header \"%s\", it should contain at least an identifier and some metadata separated by " +
                            "'%s'", fastaHeader, SECTION_DELIMITER.replace("\\", "")));
        }

        parseName(metadata, uniProtFastaHeaderData);
        parseFlags(metadata, uniProtFastaHeaderData);

        if (!uniProtFastaHeaderDataValidator.isValid(uniProtFastaHeaderData)){
            throw new FastaHeaderParseException(
                    String.format("Invalid fasta header for protein: %s", uniProtFastaHeaderData.getIdentifier()));
        }

        return uniProtFastaHeaderData;
    }

    private void parseName(String metadata, FastaHeaderData uniProtFastaHeaderData) {
        Matcher nameMatcher = fastaHeaderNamePattern.matcher(metadata);
        if (nameMatcher.matches()){
            String name = nameMatcher.group(1).trim();
            boolean isFragment = nameMatcher.group(2) != null;
            uniProtFastaHeaderData.setFragment(isFragment);
            uniProtFastaHeaderData.setName(name);
        } else {
            throw new FastaHeaderParseException(String.format("Cannot parse name from fasta header for protein: %s",
                    uniProtFastaHeaderData.getIdentifier()));
        }
    }

    private void parseFlags(String metadata, FastaHeaderData uniProtFastaHeaderData) {
        Matcher flagMatcher = fastaHeaderFlagsPattern.matcher(metadata);
        try {
            while (flagMatcher.find()) {
                String type = flagMatcher.group(1);
                String value = flagMatcher.group(2).trim();
                if (ORGANISM_SCIENTIFIC_NAME_FLAG.equals(type)) {
                    uniProtFastaHeaderData.setOrganismScientificName(value);
                } else if (ORGANISM_LINEAGE_FLAG.equals(type)) {
                    List<Integer> lineage = splitToList(value, Integer::valueOf);
                    uniProtFastaHeaderData.setOrganismLineage(lineage);
                } else if (GENE_NAME_FLAG.equals(type)) {
                    String recommendedGeneName = splitToList(value, i -> i).get(0);
                    uniProtFastaHeaderData.setRecommendedGeneName(recommendedGeneName);
                } else if (GENE_OLN_OR_ORF_FLAG.equals(type)) {
                    String recommendedOlnOrOrf = splitToList(value, i -> i).get(0);
                    uniProtFastaHeaderData.setRecommendedOlnOrOrf(recommendedOlnOrOrf);
                } else if (GENE_ORGANELLE_LOCATION_FLAG.equals(type)) {
                    List<OrganelleType> organelles = splitToList(value, OrganelleType::fromValue);
                    uniProtFastaHeaderData.setGeneLocationOrganelles(organelles);
                }
            }
        } catch (Exception e){
            throw new FastaHeaderParseException(
                    String.format("Invalid fasta header for protein: %s", uniProtFastaHeaderData.getIdentifier()), e);
        }
    }

    private static <T> List<T> splitToList(String value, Function<String, T> function){
        return Arrays.stream(value.split(MULTIVALUED_DELIMITER_REGEX)).map(function).collect(Collectors.toList());
    }

}
