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

package uk.ac.ebi.uniprot.urml.input.parsers.xml.uniparc;

import uk.ac.ebi.uniprot.urml.core.xml.schema.JAXBContextInitializationException;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.uniparc.Uniparc;

/**
 * Unmarshalls the UniParc XML using the UniParc schema model (cf. {@link org.uniprot.uniparc})
 *
 * @author Alexandre Renaux
 */
public class UniParcXmlUnmarshaller {

    private final Logger logger = LoggerFactory.getLogger(UniParcXmlUnmarshaller.class);

    private Unmarshaller unmarshaller;

    public UniParcXmlUnmarshaller() {
        try {
            JAXBContext context = JAXBContext.newInstance(Uniparc.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e){
            throw new JAXBContextInitializationException("Cannot initialize "+this.getClass().getSimpleName(), e);
        }
    }

    public Uniparc read(InputStream inputStream) throws JAXBException, IOException {
        if (inputStream == null){
            throw new IOException("Null input stream");
        }
        return ((Uniparc) unmarshaller.unmarshal(inputStream));
    }


}
