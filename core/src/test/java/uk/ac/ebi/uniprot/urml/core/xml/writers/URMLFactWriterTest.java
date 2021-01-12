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

package uk.ac.ebi.uniprot.urml.core.xml.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.uniprot.urml.facts.FactSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.uniprot.urml.facts.ProteinAnnotation.builder;

/**
 * @author Vishal Joshi
 */
class URMLFactWriterTest {

    @TempDir
    File tempDir;

    @Test
    void testShouldVerifyThatFactSuperSetIsWrittenSuccessfully() throws IOException, JAXBException, XMLStreamException {

        //given
        File outputFile = new File(tempDir.getAbsolutePath()+"/test.xml");
        try(URMLFactWriter factWriter = new URMLFactWriter(new FileOutputStream(outputFile))) {
            FactSet factSet = FactSet.builder()
                    .withFact(builder().withType("type1").build(), builder().withType("type2").build())
                    .build();

            //when and then
            assertDoesNotThrow(() -> factWriter.write(factSet));
        }

    }

}