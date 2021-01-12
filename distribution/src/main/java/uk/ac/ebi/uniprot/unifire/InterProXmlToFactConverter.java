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

import org.uniprot.urml.facts.*;
import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLWriter;
import uk.ac.ebi.uniprot.urml.input.InputType;
import uk.ac.ebi.uniprot.urml.input.parsers.FactSetParser;
import uk.ac.ebi.uniprot.urml.output.xml.IndividualFactWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Hermann Zellner on 31/07/2019.
 */
public class InterProXmlToFactConverter {

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
                    List<Fact> facts = new ArrayList<>(factSet.getFact());

                    PositionalProteinSignature positionalProteinSignature = createPositionalProteinSignatureForJ0U7L2();
                    facts.add(positionalProteinSignature);

                    for (Fact fact : facts) {
                        factWriter.writeElementWise(fact);
                    }
                    factWriter.completeWrite();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static PositionalProteinSignature createPositionalProteinSignatureForJ0U7L2() {
        PositionalProteinSignature positionalProteinSignature = new PositionalProteinSignature();
        TemplateProtein protein = new TemplateProtein();
        protein.setId("J0U7L2");
        positionalProteinSignature.setProtein(protein);

        Signature signature = new Signature();
        signature.setValue("SRHMM000005-1");
        signature.setType(SignatureType.SRHMM);
        positionalProteinSignature.setSignature(signature);

        positionalProteinSignature.setFrequency(1);

        positionalProteinSignature.setPositionStart(1);
        positionalProteinSignature.setPositionEnd(208);

        SequenceAlignment sequenceAlignment = new SequenceAlignment();
        sequenceAlignment.setValue("mlmaavlaapafpafsaget----------------PAAPTKAAAK---PDLVKGEASFTAVCAACHGADGNSAIAANPKLSAQHPEYLVKQLQEFKSG---KRNDPVMKGFAMALSDEDMKNIAYWVTAK-AAKPGFAKDKALVSLGERIYRGGIADRQIAACAGCHSPNGAGIPAQYPRLSGQHADYTATQLVAFRDG");

        positionalProteinSignature.setAlignment(sequenceAlignment);
        return positionalProteinSignature;
    }
}
