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

package uk.ac.ebi.uniprot.urml.input.parsers.uk.ac.ebi.uniprot.urml.output.column;

import uk.ac.ebi.uniprot.urml.output.column.AnnotationTSVWriter;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.uniprot.urml.facts.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by Hermann Zellner on 03/12/18.
 */
class AnnotationTSVWriterTest {

    /**
     * Created by Hermann Zellner on 03/12/18.
     */
    @DisplayName("Tests for AnnotationTSVWriter writing and reading predicted ProteinAnnotations")
    @ParameterizedTest
    @MethodSource("argumentSource")
    void test(List<ProteinAnnotation> proteinAnnotations, List<String> expectedResult) throws IOException {
        FactSet factSet = getFactSets(proteinAnnotations);

        File tempFile = File.createTempFile("annotationTSVWriter", "tsv");
        tempFile.deleteOnExit();
        try (AnnotationTSVWriter annotationTSVWriter = new AnnotationTSVWriter(new FileOutputStream(tempFile))){
            annotationTSVWriter.write(factSet);
        }

        // Read the tmpfile
        List<String> result = readResult(tempFile);

        assertThat(result, is(equalTo(expectedResult)));
    }

    private static Stream<Arguments> argumentSource() {
        return Stream.of(
                Arguments.of(
                        Arrays.asList(
                                getProteinAnnotation("A0A024QZ29", "keyword", "ANK repeat", null, null, "UR000414900")
                        ),
                        Arrays.asList(
                                "Evidence\tProteinId\tAnnotationType\tValue\tStart\tEnd",
                                "UR000414900\tA0A024QZ29\tkeyword\tANK repeat\t\t"
                        )
                ),
                Arguments.of(
                        Arrays.asList(
                                getProteinAnnotation("P12345", "feature.METAL", null, 26, 26, "UR000416798")
                        ),
                        Arrays.asList(
                                "Evidence\tProteinId\tAnnotationType\tValue\tStart\tEnd",
                                "UR000416798\tP12345\tfeature.METAL\t\t26\t26"
                        )
                ),
                Arguments.of(
                        Arrays.asList(
                                getProteinAnnotation("A8DZ02", "feature.METAL", "Zinc; catalytic", 604, 604, "UR000401086")
                        ),
                        Arrays.asList(
                                "Evidence\tProteinId\tAnnotationType\tValue\tStart\tEnd",
                                "UR000401086\tA8DZ02\tfeature.METAL\tZinc; catalytic\t604\t604"
                        )
                ),
                Arguments.of(
                        Arrays.asList(
                                getProteinAnnotation("P12345", "feature.METAL", null, 26, 26, "UR000416798"),
                                getProteinAnnotation("P12345", "feature.METAL", "Zinc", 26, 26, "UR000416798")
                        ),
                        Arrays.asList(
                                "Evidence\tProteinId\tAnnotationType\tValue\tStart\tEnd",
                                "UR000416798\tP12345\tfeature.METAL\t\t26\t26",
                                "UR000416798\tP12345\tfeature.METAL\tZinc\t26\t26"
                        )
                )
        );
    }

    private static ProteinAnnotation getProteinAnnotation(String proteinAccession, String type, String value,
                                                          Integer start,
                                                          Integer end, String evidence) {
        Protein.Builder<Void> proteinBuilder =
                Protein.builder().withId(proteinAccession).withOrganism(Organism.builder().build()).withSequence(ProteinSequence.builder().build());

        return ProteinAnnotation.builder().withProtein(proteinBuilder.build()).withValue(value)
                .withType(type).withPositionStart(start).withPositionEnd(end).withEvidence(evidence).build();
    }

    private FactSet getFactSets(Collection<ProteinAnnotation> proteinAnnotations) {
        FactSet.Builder factSetBuilder = FactSet.builder();
        proteinAnnotations.forEach(factSetBuilder::addFact);
        return factSetBuilder.build();
    }

    private List<String> readResult(File tempFile) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line = reader.readLine();
            while(line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }

        return lines;
    }

}