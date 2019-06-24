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

package uk.ac.ebi.uniprot.urml.output.xml;

import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLFactWriter;
import uk.ac.ebi.uniprot.urml.output.FactSetWriter;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.facts.ProteinAnnotation;

/**
 *  Writes {@link ProteinAnnotation}s from a {@link FactSet} into XML format.
 *
 * @author Alexandre Renaux
 */
public class AnnotationXMLWriter implements FactSetWriter<ProteinAnnotation> {

    private final URMLFactWriter urmlFactWriter;
    private final XMLStreamWriter xmlStreamWriter;

    public AnnotationXMLWriter(OutputStream outputStream) {
        this.urmlFactWriter = new URMLFactWriter();
        try {
            this.xmlStreamWriter = urmlFactWriter.initStream(outputStream, FactSet.builder().build());
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException("Cannot initialize XML stream output");
        }
    }

    @Override
    public void write(FactSet factSet) {
        factSet.getFact().forEach(f -> write(((ProteinAnnotation)f)));
    }

    @Override
    public void write(ProteinAnnotation proteinAnnotation) {
        try {
            urmlFactWriter.marshallElement(proteinAnnotation, xmlStreamWriter);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Error writing annotation "+proteinAnnotation, e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            urmlFactWriter.write(xmlStreamWriter);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
}
