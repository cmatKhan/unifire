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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.OrganelleType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit tests for {@link FastaHeaderParser}
 *
 * @author Alexandre Renaux
 */
class FastaHeaderParserTest {

    private static final String MINIMAL_HEADER = "1|name OX=1,2,3";

    @Test
    void parseWithMinimalValidHeader() throws Exception {
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(MINIMAL_HEADER);
        assertMinimalHeaderData(parsedData);
    }

    @Test
    void parseWithDatabaseValidHeader() throws Exception {
        String minimalHeader = "db|"+MINIMAL_HEADER;
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);
        assertMinimalHeaderData(parsedData);
    }

    @Test
    void parseWithAngleBracketValidHeader() throws Exception {
        String minimalHeader = ">"+MINIMAL_HEADER;
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);
        assertMinimalHeaderData(parsedData);
    }

    @Test
    void parseWithAdditionalUnsupportedFlagValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" PE=1";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);
        assertMinimalHeaderData(parsedData);
    }


    @Test
    void parseWithOrganismScientificNameValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" OS=orgName";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.getOrganismScientificName(), equalTo("orgName"));

    }

    @Test
    void parseWithFragmentValidHeader() throws Exception {
        String minimalHeader = ">1|name (Fragment) OX=1,2,3";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.isFragment(), equalTo(true));
    }

    @Test
    void parseWithGeneNameValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" GN=abc";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.getRecommendedGeneName(), equalTo("abc"));
    }

    @Test
    void parseWithOlnOrOrfValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" GL=def";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.getRecommendedOlnOrOrf(), equalTo("def"));
    }

    @Test
    void parseWithMultipleOlnOrOrfKeepFirstValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" GL=def,ghi";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.getRecommendedOlnOrOrf(), equalTo("def"));
    }

    @Test
    void parseWithSingleGeneLocationOrganelleValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" OG=plastid";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.getGeneLocationOrganelles(), containsInAnyOrder(OrganelleType.PLASTID));
    }

    @Test
    void parseWithMultipleGeneLocationOrganelleValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" OG=plastid,chloroplast";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.getGeneLocationOrganelles(), containsInAnyOrder(OrganelleType.PLASTID, OrganelleType.CHLOROPLAST));
    }

    @Test
    void parseWithMultipleGeneLocationOrganelleSpacedDelimiterValidHeader() throws Exception {
        String minimalHeader = MINIMAL_HEADER+" OG=plastid, chloroplast";
        FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
        FastaHeaderData parsedData = fastaHeaderParser.parse(minimalHeader);

        assertMinimalHeaderData(parsedData);
        assertThat(parsedData.getGeneLocationOrganelles(), containsInAnyOrder(OrganelleType.PLASTID, OrganelleType.CHLOROPLAST));
    }

    @Test
    void parseWithInvalidGeneLocationOrganelleInvalidHeader() throws Exception {
        Assertions.assertThrows(FastaHeaderParseException.class, () -> {
            String minimalHeader = MINIMAL_HEADER + " OG=invalidOrganelle";
            FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
            fastaHeaderParser.parse(minimalHeader);
        });
    }

    @Test
    void parseWithoutFlagsInvalidHeader() throws Exception {
        Assertions.assertThrows(FastaHeaderParseException.class, () -> {
            String minimalHeader = ">1|name";
            FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
            fastaHeaderParser.parse(minimalHeader);
        });
    }

    @Test
    void parseWithoutOXFlagInvalidHeader() throws Exception {
        Assertions.assertThrows(FastaHeaderParseException.class, () -> {
            String minimalHeader = ">1|name OS=orgName";
            FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
            fastaHeaderParser.parse(minimalHeader);
        });
    }

    @Test
    void parseWithoutName() throws Exception {
        Assertions.assertThrows(FastaHeaderParseException.class, () -> {
            String minimalHeader = ">1| OS=orgName";
            FastaHeaderParser fastaHeaderParser = new FastaHeaderParser();
            fastaHeaderParser.parse(minimalHeader);
        });
    }

    private void assertMinimalHeaderData(FastaHeaderData parsedData){
        assertThat(parsedData.getIdentifier(), equalTo("1"));
        assertThat(parsedData.getName(), equalTo("name"));
        assertThat(parsedData.getOrganismLineage(), containsInAnyOrder(1, 2, 3));
    }

}