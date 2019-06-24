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

package uk.ac.ebi.uniprot.urml.input.parsers.uniparc.xml;

/**
 * Exception raised when the UniParc format is incorrect or an important information is missing.
 *
 * @author Alexandre Renaux
 */
public class UniParcXmlFormatException extends RuntimeException {

    public UniParcXmlFormatException(String message) {
        super(message);
    }

    public UniParcXmlFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniParcXmlFormatException(Throwable cause) {
        super(cause);
    }

    public UniParcXmlFormatException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
