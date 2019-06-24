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

import java.util.List;
import org.uniprot.urml.facts.OrganelleType;

/**
 * Stores all the data contained in the custom fasta header
 *
 * @author Alexandre Renaux
 */
public class FastaHeaderData {

    private final String rawData;
    private String database;
    private String identifier;
    private String name;
    private Boolean isFragment;
    private String organismScientificName;
    private List<Integer> organismLineage;
    private String recommendedGeneName;
    private String recommendedOlnOrOrf;
    private List<OrganelleType> geneLocationOrganelles;

    public FastaHeaderData(String rawData) {
        this.rawData = rawData;
    }

    public String getDatabase() {
        return database;
    }

    public String getRawData() {
        return rawData;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public Boolean isFragment() {
        return isFragment;
    }

    public String getOrganismScientificName() {
        return organismScientificName;
    }

    public List<Integer> getOrganismLineage() {
        return organismLineage;
    }

    public String getRecommendedGeneName() {
        return recommendedGeneName;
    }

    public String getRecommendedOlnOrOrf() {
        return recommendedOlnOrOrf;
    }

    public List<OrganelleType> getGeneLocationOrganelles() {
        return geneLocationOrganelles;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFragment(Boolean fragment) {
        isFragment = fragment;
    }

    public void setOrganismScientificName(String organismScientificName) {
        this.organismScientificName = organismScientificName;
    }

    public void setOrganismLineage(List<Integer> organismLineage) {
        this.organismLineage = organismLineage;
    }

    public void setRecommendedGeneName(String recommendedGeneName) {
        this.recommendedGeneName = recommendedGeneName;
    }

    public void setRecommendedOlnOrOrf(String recommendedOlnOrOrf) {
        this.recommendedOlnOrOrf = recommendedOlnOrOrf;
    }

    public void setGeneLocationOrganelles(List<OrganelleType> geneLocationOrganelles) {
        this.geneLocationOrganelles = geneLocationOrganelles;
    }
}
