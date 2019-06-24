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

import java.io.*;
import java.text.ParseException;

/**
 * Validates fasta header from a multifasta source
 *
 * @author Alexandre Renaux
 */
public class MultiFastaHeaderValidator {

    private final FastaHeaderParser fastaHeaderParser;

    public MultiFastaHeaderValidator() {
        this.fastaHeaderParser = new FastaHeaderParser();
    }

    public boolean isValid(File multiFastaFile) throws IOException, ParseException {
        return isValid(new FileInputStream(multiFastaFile));
    }

    public boolean isValid(InputStream multiFastaIS) throws IOException, ParseException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(multiFastaIS))) {
            String line;
            int lineNumber = 1;
            while ((line = in.readLine()) != null) {
                if (line.startsWith(">")) {
                    try {
                        fastaHeaderParser.parse(line);
                    } catch (FastaHeaderParseException e){
                        throw new ParseException(e.getLocalizedMessage(), lineNumber);
                    }
                }
                lineNumber++;
            }
        }
        return true;
    }
}
