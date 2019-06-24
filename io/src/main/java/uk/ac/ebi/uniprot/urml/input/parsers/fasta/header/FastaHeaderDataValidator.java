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

import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic validator to ensure the minimal header data are present
 *
 * @author Alexandre Renaux
 */
public class FastaHeaderDataValidator {

    private static final Logger logger = LoggerFactory.getLogger(FastaHeaderDataValidator.class);

    public boolean isValid(FastaHeaderData uniProtFastaHeaderData){
        if (uniProtFastaHeaderData.getIdentifier() == null) {
            logger.error("Missing identifier data for protein {}", uniProtFastaHeaderData.getRawData());
            return false;
        } else if (CollectionUtils.isEmpty(uniProtFastaHeaderData.getOrganismLineage())) {
            logger.error("Missing lineage data for protein {}", uniProtFastaHeaderData.getRawData());
            return false;
        } else if (uniProtFastaHeaderData.getOrganismLineage().size() == 1){
            logger.warn("Missing full lineage data for protein {}", uniProtFastaHeaderData.getRawData());
        } else if (Strings.isNullOrEmpty(uniProtFastaHeaderData.getOrganismScientificName())){
            logger.warn("Missing organism name for protein {}", uniProtFastaHeaderData.getRawData());
        }
        return true;
    }

}
