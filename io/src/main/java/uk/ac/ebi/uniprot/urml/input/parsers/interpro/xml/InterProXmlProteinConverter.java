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

package uk.ac.ebi.uniprot.urml.input.parsers.interpro.xml;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.uniprot.urml.input.parsers.fasta.header.FastaHeaderData;
import uk.ac.ebi.uniprot.urml.input.parsers.fasta.header.FastaHeaderParser;

import java.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.urml.facts.Signature;
import org.uniprot.urml.facts.*;

/**
 * Iterates over {@link Protein} and convert them to {@link org.uniprot.urml.facts.FactSet}.
 *
 * @author Alexandre Renaux
 */
public class InterProXmlProteinConverter implements Iterator<FactSet>{

    private static final Logger logger = LoggerFactory.getLogger(InterProXmlProteinConverter.class);

    private final Iterator<Protein> sourceIterator;
    private final Map<String, Organism> organismMap;
    private final FastaHeaderParser uniProtFastaHeaderParser;
    private final Queue<FactSet> factSetQueue;


    public InterProXmlProteinConverter(ProteinMatchesHolder proteinMatches){
        this.sourceIterator = proteinMatches.getProteins().iterator();
        this.organismMap = new HashMap<>();
        this.uniProtFastaHeaderParser = new FastaHeaderParser();
        this.factSetQueue = new LinkedList<>();
    }

    @Override
    public boolean hasNext() {
        return !factSetQueue.isEmpty() || sourceIterator.hasNext();
    }

    @Override
    public FactSet next() {
        if (factSetQueue.isEmpty() && !sourceIterator.hasNext()){
            throw new NoSuchElementException();
        } else {
            if (factSetQueue.isEmpty()) {
                convertProteinMatches(sourceIterator.next());
            }
            return factSetQueue.poll();
        }
    }

    private void convertProteinMatches(Protein ipsProtein){

        if (!ipsProtein.getCrossReferences().isEmpty()) {
            Iterator<ProteinXref> xrefIterator = ipsProtein.getCrossReferences().iterator();
            while (xrefIterator.hasNext()) {
                ProteinXref proteinXref = xrefIterator.next();
                FastaHeaderData fastaHeaderData = uniProtFastaHeaderParser.parse(proteinXref.getName());
                FactSet.Builder<Void> factSetBuilder = FactSet.builder();

                org.uniprot.urml.facts.Protein.Builder<Void> proteinBuilder = org.uniprot.urml.facts.Protein.builder();
                buildProtein(proteinBuilder, fastaHeaderData, ipsProtein);
                buildGeneInformation(proteinBuilder, fastaHeaderData);
                Organism organism = buildOrganism(factSetBuilder, fastaHeaderData);

                proteinBuilder.withOrganism(organism);
                org.uniprot.urml.facts.Protein protein = proteinBuilder.build();
                factSetBuilder.addFact(protein);

                for (Match match : ipsProtein.getMatches()) {
                    buildProteinSignature(factSetBuilder, protein, match);
                }

                factSetQueue.add(factSetBuilder.build());
            }
        } else {
            throw new InterProScanXmlFormatException(
                    String.format("Missing xref tag for ipsProtein md5=%s", ipsProtein.getMd5()));
        }

    }

    private void buildProteinSignature(FactSet.Builder<Void> factSetBuilder, org.uniprot.urml.facts.Protein protein,
            Match match) {
        if (match.getSignature().getEntry() == null){
            logger.debug("Ignored unintegrated InterPro signature {}", match.getSignature().getAccession());
            return;
        }
        String accession = match.getSignature().getAccession();
        SignatureLibrary library = match.getSignature().getSignatureLibraryRelease().getLibrary();
        SignatureType signatureType = getSignatureType(library);
        if (signatureType == null){
            logger.warn("Ignored signature type {}", library.getName());
            return;
        }
        Signature libSignature = Signature.builder().withType(signatureType).withValue(accession).build();
        String ipsAccession = match.getSignature().getEntry().getAccession();
        Signature ipSignature = Signature.builder().withType(SignatureType.INTER_PRO).withValue(ipsAccession).build();

        for (Object o : match.getLocations()) {
            PositionalProteinSignature.Builder<Void> pSignatureBuilder = PositionalProteinSignature.builder();
            pSignatureBuilder.withProtein(protein);
            pSignatureBuilder.withFrequency(match.getLocations().size());
            if (o instanceof ProfileScanMatch.ProfileScanLocation) {
                ProfileScanMatch.ProfileScanLocation profileScanLocation = (ProfileScanMatch.ProfileScanLocation) o;
                String alignment = profileScanLocation.getAlignment();
                pSignatureBuilder.withAlignment().withValue(alignment).end();
            }
            if (o instanceof Location) {
                Location location = (Location) o;
                pSignatureBuilder.withPositionStart(location.getStart());
                pSignatureBuilder.withPositionEnd(location.getEnd());
            }
            factSetBuilder.addFact(pSignatureBuilder.withSignature(libSignature).build());
            factSetBuilder.addFact(pSignatureBuilder.withSignature(ipSignature).build());
        }
    }

    private Organism buildOrganism(FactSet.Builder<Void> factSetBuilder, FastaHeaderData fastaHeaderData) {
        Organism organism = createOrGetOrganism(fastaHeaderData.getOrganismScientificName(),
                                                fastaHeaderData.getOrganismLineage());
        factSetBuilder.addFact(organism);
        return organism;
    }

    private void buildProtein(org.uniprot.urml.facts.Protein.Builder proteinBuilder, FastaHeaderData fastaHeaderData,
            Protein ipsProtein) {
        proteinBuilder.withId(fastaHeaderData.getIdentifier());
        proteinBuilder.withSequence().withValue(ipsProtein.getSequence())
                .withLength(ipsProtein.getSequenceLength()).withIsFragment(fastaHeaderData.isFragment()).end();
    }

    private void buildGeneInformation(org.uniprot.urml.facts.Protein.Builder proteinBuilder,
            FastaHeaderData fastaHeaderData) {
        GeneInformation.Builder geneBuilder = proteinBuilder.withGene();

        if(fastaHeaderData.getRecommendedGeneName() != null){
            geneBuilder.withNames(fastaHeaderData.getRecommendedGeneName());
        }
        if (fastaHeaderData.getRecommendedOlnOrOrf() != null){
            geneBuilder.withOrfOrOlnNames(fastaHeaderData.getRecommendedOlnOrOrf());
        }
        if (!CollectionUtils.isEmpty(fastaHeaderData.getGeneLocationOrganelles())){
            geneBuilder.withOrganelleLocations(fastaHeaderData.getGeneLocationOrganelles());
        }
    }

    private Organism createOrGetOrganism(String scientificName, List<Integer> taxIdLineage){
        String key = "organism_"+taxIdLineage.get(taxIdLineage.size() - 1).toString();
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

    private SignatureType getSignatureType(SignatureLibrary signatureLibrary) {
        switch (signatureLibrary){
            case CDD:
                return SignatureType.CDD;
            case PFAM:
                return SignatureType.PFAM;
            case SFLD:
                return SignatureType.SFLD;
            case GENE3D:
                return SignatureType.GENE_3_D;
            case HAMAP:
                return SignatureType.HAMAP;
            case PANTHER:
                return SignatureType.PANTHER;
            case PIRSF:
                return SignatureType.PIR_SUPERFAMILY;
            case PRINTS:
                return SignatureType.PRINTS;
            case PRODOM:
                return SignatureType.PRO_DOM;
            case SMART:
                return SignatureType.SMART;
            case TIGRFAM:
                return SignatureType.TIGRFAM;
            case SUPERFAMILY:
                return SignatureType.SCOP_SUPERFAMILY;
            case PROSITE_PATTERNS: case PROSITE_PROFILES:
                return SignatureType.PROSITE;
            default:
                return null;
        }
    }

}
