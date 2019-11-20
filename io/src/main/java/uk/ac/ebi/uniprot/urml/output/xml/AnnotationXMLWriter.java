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

import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.facts.ProteinAnnotation;
import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLFactWriter;
import uk.ac.ebi.uniprot.urml.core.xml.writers.URMLWriter;
import uk.ac.ebi.uniprot.urml.output.FactSetWriter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes {@link ProteinAnnotation}s from a {@link FactSet} into XML format.
 *
 * @author Alexandre Renaux
 */
public class AnnotationXMLWriter implements FactSetWriter<ProteinAnnotation> {

    private final URMLWriter urmlFactWriter;

    public AnnotationXMLWriter(OutputStream outputStream) {
        try {
            this.urmlFactWriter = new URMLFactWriter(outputStream);
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException("Cannot initialize XML stream output");
        } catch (JAXBException jaxbException) {
            throw new IllegalArgumentException(jaxbException.getMessage());
        }
    }

    @Override
    public void write(FactSet factSet) {
        factSet.getFact().forEach(f -> write(((ProteinAnnotation) f)));
        try {
            urmlFactWriter.completeWrite();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(ProteinAnnotation proteinAnnotation) {
        try {
            urmlFactWriter.writeElementWise(proteinAnnotation);
            urmlFactWriter.completeWrite();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Error writing annotation " + proteinAnnotation, e);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            urmlFactWriter.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
