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

import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLWriter;
import uk.ac.ebi.uniprot.urml.input.InputType;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetParser;
import uk.ac.ebi.uniprot.urml.output.xml.IndividualFactWriter;

import java.io.*;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.*;

/**
 * Created by Hermann Zellner on 31/07/2019.
 */
public class InterProXmlToFactConverter {

    private static final Logger logger = LoggerFactory.getLogger(InterProXmlToFactConverter.class);

    public static void main(String[] argv) throws IOException {
        InterProXmlToFactConverter converter = new InterProXmlToFactConverter();
        converter.convertToXml(argv[0], argv[1]);
    }

    public void convertToXml(String inputFileName, String outputFileName) throws IOException {
        try (InputStream factInputStream = new FileInputStream(inputFileName);
             OutputStream outputStream = new FileOutputStream(outputFileName)) {
            Iterator<FactSet> factSetIterator = FactSetParser.of(InputType.INTERPROSCAN_XML).parse(factInputStream);
            try (URMLWriter<FactSet, Fact> factWriter = new IndividualFactWriter(outputStream)) {
                while (factSetIterator.hasNext()) {
                    FactSet factSet = factSetIterator.next();

                    for (Fact fact : factSet.getFact()) {
                        factWriter.writeElementWise(fact);
                    }
                    factWriter.completeWrite();
                }

            } catch (Exception e) {
                logger.error("Error while converting InterPro-XML to Fact-XML", e);
            }
        }
    }
}
