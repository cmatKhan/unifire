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

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.uniparc.*;
import org.uniprot.urml.facts.*;

/**
 * Iterates over {@link org.uniprot.uniparc.Entry} and convert them to {@link org.uniprot.urml.facts.FactSet}.
 *
 * @author Alexandre Renaux
 */
public class UniParcXmlEntryConverter implements Iterator<FactSet> {

    private final static Logger logger = LoggerFactory.getLogger(UniParcXmlEntryConverter.class);
    private final Iterator<Entry> sourceIterator;
    private final Map<String, Organism> organismMap;

    public UniParcXmlEntryConverter(Uniparc uniparc) {
        this.sourceIterator = uniparc.getEntry().iterator();
        this.organismMap = new HashMap<>();
    }

    @Override
    public boolean hasNext() {
        return sourceIterator.hasNext();
    }

    @Override
    public FactSet next() {
        Entry uniparcEntry = sourceIterator.next();
        String accession = uniparcEntry.getAccession();
        String dbReferenceId = null;
        String ncbiTaxIds = null;
        String ncbiTaxId = null;
        String geneName = null;
        String organismScientificName = null;
        if (uniparcEntry.getDbReference().size() > 1){
            throw new UniParcXmlFormatException("UniParc XML should be preprocessed to keep only 1 dbReference (accession="+accession+").");
        }
        for (DbReferenceType dbReference : uniparcEntry.getDbReference()) {
            dbReferenceId = dbReference.getId();
            for (PropertyType property : dbReference.getProperty()) {
                switch (property.getType()){
                    case "NCBI_taxonomy_id":
                        ncbiTaxId = property.getValue();
                    case "NCBI_taxonomy_lineage_ids":
                        ncbiTaxIds = property.getValue();
                        break;
                    case "organism_scientific_name":
                        organismScientificName = property.getValue();
                        break;
                    case "gene_name":
                        geneName = property.getValue();
                        break;
                }
            }
        }

        if (dbReferenceId == null){
            throw new UniParcXmlFormatException("Missing dbReference for accession="+accession);
        }

        FactSet.Builder<Void> factSetBuilder = FactSet.builder();

        Iterable<Integer> taxIdList;
        if (ncbiTaxIds != null){
            taxIdList = Ints.stringConverter().convertAll(Splitter.on(",").split(ncbiTaxIds));
        } else {
            throw new UniParcXmlFormatException("UniParc XML should be preprocessed to have dbReference property 'NCBI_taxonomy_lineage_ids' (accession="+accession+")");
        }
        Organism organism = createOrGetOrganism(ncbiTaxId, organismScientificName, taxIdList);
        factSetBuilder.addFact(organism);

        Sequence sequence = uniparcEntry.getSequence();
        ProteinSequence proteinSequence =
                ProteinSequence.builder().withValue(sequence.getContent()).withLength(sequence.getLength()).build();

        GeneInformation.Builder<Void> geneInformationBuilder = GeneInformation.builder();
        if (geneName != null){
            geneInformationBuilder.withNames(geneName);
        }
        GeneInformation geneInformation = geneInformationBuilder.build();

        String combinedProteinId = accession+":"+dbReferenceId;
        Protein protein = Protein.builder().withId(combinedProteinId).withSequence(proteinSequence).withOrganism(organism)
                .withGene(geneInformation).build();
        factSetBuilder.addFact(protein);

        for (SeqFeatureType seqFeatureType : uniparcEntry.getSignatureSequenceMatch()) {
            SignatureType signatureType = getSignatureType(seqFeatureType.getDatabase());
            if (signatureType == null){
                logger.warn("Ignored signature type {}", seqFeatureType.getDatabase());
                continue;
            }
            String libSignatureId = getSignatureValue(seqFeatureType.getId(), signatureType);
            String iprSignatureId = seqFeatureType.getIpr().getId();
            if (iprSignatureId == null){
                logger.debug("Ignored unintegrated InterPro signature {}", libSignatureId);
                continue;
            }

            Signature libSignature = Signature.builder().withType(signatureType).withValue(libSignatureId).build();
            Signature iprSignature = Signature.builder().withType(SignatureType.INTER_PRO).withValue(iprSignatureId).build();
            for (LocationType locationType : seqFeatureType.getLcn()) {
                ProteinSignature.Builder<Void> pSignatureBuilder = ProteinSignature.builder();
                pSignatureBuilder.withProtein(protein);
                pSignatureBuilder.withFrequency(seqFeatureType.getLcn().size());
                factSetBuilder.addFact(pSignatureBuilder.withSignature(libSignature).build());
                factSetBuilder.addFact(pSignatureBuilder.withSignature(iprSignature).build());
            }
        }

        return factSetBuilder.build();
    }

    private Organism createOrGetOrganism(String id, String scientificName, Iterable<Integer> taxIdLineage){
        String key = "organism_"+id;
        if (organismMap.containsKey(key)){
            return organismMap.get(key);
        } else {
            Organism.Builder<Void> organismBuilder = Organism.builder();
            organismBuilder.withId(key).withLineage().withIds(taxIdLineage).end();
            if (scientificName != null){
                organismBuilder.withScientificName(scientificName);
            }
            Organism organism = organismBuilder.build();
            organismMap.put(key, organism);
            return organism;
        }
    }

    private String getSignatureValue(String value, SignatureType signatureType){
        switch (signatureType){
            case GENE_3_D:
                return value.replace("G3DSA:", "");
            default:
                return value;
        }
    }

    private SignatureType getSignatureType(String library){
        switch (library){
            case "CDD":
                return SignatureType.CDD;
            case "Gene3D":
                return SignatureType.GENE_3_D;
            case "HAMAP":
                return SignatureType.HAMAP;
            case "PANTHER":
                return SignatureType.PANTHER;
            case "PIRSF":
                return SignatureType.PIR_SUPERFAMILY;
            case "Pfam":
                return SignatureType.PFAM;
            case "ProDom":
                return SignatureType.PRO_DOM;
            case "PRINTS":
                return SignatureType.PRINTS;
            case "PROSITE":
                return SignatureType.PROSITE;
            case "SFLD":
                return SignatureType.SFLD;
            case "SMART":
                return SignatureType.SMART;
            case "SUPFAM":
                return SignatureType.SCOP_SUPERFAMILY;
            case "TIGRFAMs":
                return SignatureType.TIGRFAM;
            default:
                return null;
        }
    }
}
