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

package uk.ac.ebi.uniprot.unifire.validators.fasta;

import uk.ac.ebi.uniprot.urml.input.parsers.fasta.header.MultiFastaHeaderValidator;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;

/**
 * Validates a multifasta file headers.
 *
 * @author Alexandre Renaux
 */
public class MultiFastaValidatorApp {

    private static final Logger logger = LoggerFactory.getLogger(MultiFastaValidatorApp.class);


    public static void main(String[] args) throws IOException {
        if (args.length < 1){
            logger.error("Missing argument: multifasta file path.");
            exit(1);
        }
        String inputFilePath = args[0];

        MultiFastaHeaderValidator multiFastaHeaderValidator = new MultiFastaHeaderValidator();
        try {
            if (multiFastaHeaderValidator.isValid(new File(inputFilePath))) {
                logger.info("{} successfully validated.", inputFilePath);
            }
        } catch (ParseException e){
            logger.error("Invalid input: {} at line {}", e.getMessage(), e.getErrorOffset());
            exit(1);
        }
    }

}
