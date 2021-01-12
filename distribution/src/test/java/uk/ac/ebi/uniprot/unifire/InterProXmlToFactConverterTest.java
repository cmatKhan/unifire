/*
 * Copyright (c) 2018 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.uniprot.unifire;

import matchers.NodeMatcherBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Vishal Joshi
 */
class InterProXmlToFactConverterTest {

    @TempDir
    File tempDir;

    @Test
    void testShouldVerifyThatInterproXmlIsConvertedToFactXmlCorrectly() throws IOException {

        //given
        InterProXmlToFactConverter converter = new InterProXmlToFactConverter();

        ClassLoader classLoader = getClass().getClassLoader();
        String inputFileName = "iprscan.xml";
        File inputFile = new File(classLoader.getResource(inputFileName).getFile());
        String absolutePath = inputFile.getAbsolutePath();

        String outputFileName = tempDir.getAbsolutePath() + "/output.xml";
        File expectedFile = Paths.get(classLoader.getResource("correct_facts.xml").getPath()).toFile();

        //when
        converter.convertToXml(absolutePath, outputFileName);

        //then
        assertThat(new File(outputFileName), CompareMatcher.isSimilarTo(expectedFile).ignoreWhitespace().normalizeWhitespace().withNodeMatcher(NodeMatcherBuilder.factXMLNodeMatcher()).withNodeFilter(node -> !node.getNodeName().equals("#comment")));
    }

}