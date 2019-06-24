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

package uk.ac.ebi.uniprot.urml.input.parsers.fasta.header;

/**
 * Exception to be thrown when the fasta header cannot be parsed / or mandatory data are missing.
 *
 * @author Alexandre Renaux
 */
public class FastaHeaderParseException extends RuntimeException {

    public FastaHeaderParseException(String message) {
        super(message);
    }

    public FastaHeaderParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastaHeaderParseException(Throwable cause) {
        super(cause);
    }

    public FastaHeaderParseException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
