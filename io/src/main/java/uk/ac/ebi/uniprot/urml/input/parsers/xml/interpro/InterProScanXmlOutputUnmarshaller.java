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

package uk.ac.ebi.uniprot.urml.input.parsers.xml.interpro;

import uk.ac.ebi.interpro.scan.model.ProteinMatchesHolder;
import uk.ac.ebi.uniprot.urml.core.xml.schema.JAXBContextInitializationException;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Unmarshalls the InterProScan output XML using the interpro schema model (cf. {@link uk.ac.ebi.interpro.scan.model})
 *
 * @author Alexandre Renaux
 */
public class InterProScanXmlOutputUnmarshaller {

    private Unmarshaller unmarshaller;

    public InterProScanXmlOutputUnmarshaller() {
        try {
            JAXBContext context = JAXBContext.newInstance(ProteinMatchesHolder.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e){
            throw new JAXBContextInitializationException("Cannot initialize "+this.getClass().getSimpleName(), e);
        }
    }

    public ProteinMatchesHolder read(InputStream inputStream) throws JAXBException, IOException {
        if (inputStream == null){
            throw new IOException("Null input stream");
        }
        return ((ProteinMatchesHolder) unmarshaller.unmarshal(inputStream));
    }


}
