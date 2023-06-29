/*
 * Copyright (c) 2018 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.uniprot.urml.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.uniprot.urml.facts.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.uniprot.urml.facts.SignatureType.*;

/**
 * Created by Hermann Zellner on 21/07/2020.
 */
public class FactMergerTest {

    private static Organism organism_1;
    private static Organism organism_2;
    private static String sequence_1 =
            "MSNLLKLGRGLTVAVITTSVLSGCSYVIKSGANIALNFGENHIVPPILKMDDVDMICNSGSSLTPVVMSTKDMGADPTRMAVLLYAASGMCAENQALEQELRYLRASKAGQVSEAQDARIEQKRWAAVAAERQYSGYQLFAKRWETKYKYHLGDSCPTMRNDLDQTIYMLGLISGLQAVTNDINSGGAVNVPKDIAGIVERSMTCLDNNKYWGVPNATRAVIWTLLPGAGDGKPDPYVTLKQSVQIGEQKGVRLSHALYAVAAQASGDDSKIRDALKTYDASRADDKPVNPDFKLLDAMAGVMIQGISDRYWTEHTGIRTEDGGMSTFWDEQNKSSELEDLFNSDGGAASEPAASDKTAAK";;
    private static String sequence_2 =
            "MNKDHFLISRIIFIAWCASISACMQVFSDRFIYWREDLPQQFWRFWTAHWVHVGWMHFVLNMLAFACLPFIFPQSKNWQLLILILCISPIISLGFYWFMPYISAYAGFSGVLHGLYVAVALVSLKYKKERNFAGLVLGLVIAKIVWENTFGNTGTAQLIGSPVLIESHLLGALSGALAGSVYLCWIKLKVRVS";
    private static Protein protein_1;
    private static Protein protein_2;
    private static Protein protein_3;
    private static ProteinSignature proteinSignature_1_1;
    private static ProteinSignature proteinSignature_1_2;
    private static ProteinSignature proteinSignature_2_1;
    private static ProteinSignature proteinSignature_2_2;
    private static ProteinSignature proteinSignature_3_1;
    private static ProteinSignature proteinSignature_3_2;

    private static PositionalProteinSignature positionalProteinSignature_1;
    private static PositionalProteinSignature positionalProteinSignature_2;

    FactMerger factMerger = new FactMerger();

    @BeforeAll
    public static void setUp() {
        organism_1 = createOrganism("1", "Acinetobacter baylyi ADP1",
                Arrays.asList(1, 131567, 2, 1224, 1236, 72274, 468, 469, 202950, 62977));
        organism_2 = createOrganism("1", "Pasteurella multocida (strain Pm70)",
                Arrays.asList(1,131567,2,1224,1236,135625,712,745,747,44283,272843));
        protein_1 = createProtein("Q6F7U4", organism_1, sequence_1, "ACIAD3186");
        protein_2 = createProtein("Q6F7U5", organism_1, sequence_2, "ACIAD3185");
        protein_3 = createProtein("Q6F7U5", organism_2, sequence_2, "ACIAD3185");
        proteinSignature_1_1 = createProteinSignature(protein_1, GENE_3_D, "G3DSA:1.20.1540.10", 1);
        proteinSignature_1_2 = createProteinSignature(protein_1, PFAM, "PF01694", 1);
        proteinSignature_2_1 = createProteinSignature(protein_2, NCBIFAM, "TIGR03902", 1);
        proteinSignature_2_2 = createProteinSignature(protein_2, INTER_PRO, "IPR022764", 1);
        proteinSignature_3_1 = createProteinSignature(protein_3, NCBIFAM, "TIGR03902", 1);
        proteinSignature_3_2 = createProteinSignature(protein_3, INTER_PRO, "IPR022764", 1);

        positionalProteinSignature_1 = createPositionalProteinSignature(
                protein_1, SRHMM, "SRHMM000077-4", 1, 1, 141,
                "mivvcpscgaknrvpenklaeqpqcgqchakllplap---IELNEQNFSHFITYSDLPVLIDLWAEWCGPCKMMAPHFAHVAAQ-NPQVIFAKINTETSPRLSQAFHVRSIPTLVLMNKTTEIARISGALRSTELQQWLDQQ-lhs"
        );
        positionalProteinSignature_2 = createPositionalProteinSignature(
                protein_3, SRHMM, "SRHMM006247-1", 1, 1, 487,
                "m--RILSIIRIVGILVMCFSLTMLAPAFVALLYGDGGGKAFMQTFVMSAIVGMLLWWPCHHHKEE-LRSRDGFLIVVAFWLVLGSIGAIPFMLFEKPHLSFSSAIFESFSGLTTTGATVIEGLDQLPKAILFYRQLLQWLGGMGIIVLAVAIIPLLGIGGTQLYRAESSGPlKEQKLRPRIAEVAKLLWILYFSLTVLCAIAYWFAGMNAFDAIGHSFSTVANGGFSTHDASMGYFNNATIYLITTFFMLIAGVNFNLHISALTylGKQslwKNYWKDPEFRFFVAIQVLFILLFSLSLYFYDVLTNLSDAFIQGSLQLTSMSMTAGYSIFDMNNLPAFSAMLLVIASVIGGCGGSTTGGLKTIRVLILWLQVKRELRSLVHPNLVQPIKLGQNILPIRMLESIWAFLMIFILVYWVCVFAVILCGMDVFDAMGSVFATLTNAGPGLG--VIHQDFLNVPESAKIVFAFAMICGRLEIFSLLVLFTPTFWK-e"
        );
    }

    @Test
    public void emptyFactSetMergedWithEmptyMap() {
        FactSet factSet = new FactSet();
        Iterator<FactSet> factSetIterator = Arrays.asList(factSet).iterator();

        List<PositionalProteinSignature> factsToAdd = new ArrayList<>();

        List<Fact> mergedFacts = factMerger.merge(factSetIterator, factsToAdd);

        assertThat(mergedFacts.size(), is(0));
    }

    @Test
    public void emtryFactSetMergedWithOneAlignment() {
        FactSet factSet = new FactSet();
        Iterator<FactSet> factSetIterator = Arrays.asList(factSet).iterator();

        List<PositionalProteinSignature> factsToAdd = Arrays.asList(positionalProteinSignature_1);

        List<Fact> mergedFacts = factMerger.merge(factSetIterator, factsToAdd);

        assertThat(mergedFacts.size(), is(1));
        assertThat(mergedFacts, containsInAnyOrder(positionalProteinSignature_1));
    }

    @Test
    public void oneProteinMergedWithEmptyMap() {
        FactSet factSet = new FactSet();
        factSet.setFact(Arrays.asList(protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2));
        Iterator<FactSet> factSetIterator = Arrays.asList(factSet).iterator();

        List<PositionalProteinSignature> factsToAdd = new ArrayList<>();

        List<Fact> mergedFacts = factMerger.merge(factSetIterator, factsToAdd);

        assertThat(mergedFacts.size(), is(4));
        assertThat(mergedFacts, containsInAnyOrder(
                protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2
        ));
    }

    @Test
    public void oneProteinMergedWithOneAlignment() {
        FactSet factSet = new FactSet();
        factSet.setFact(Arrays.asList(protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2));
        Iterator<FactSet> factSetIterator = Arrays.asList(factSet).iterator();

        List<PositionalProteinSignature> factsToAdd = Arrays.asList(positionalProteinSignature_1);

        List<Fact> mergedFacts = factMerger.merge(factSetIterator, factsToAdd);

        assertThat(mergedFacts.size(), is(5));
        assertThat(mergedFacts, containsInAnyOrder(protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2,
                positionalProteinSignature_1));
    }

    @Test
    public void oneProteinNotMergedWithAlignmentForOtherProtein() {
        FactSet factSet = new FactSet();
        factSet.setFact(Arrays.asList(protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2));
        Iterator<FactSet> factSetIterator = Arrays.asList(factSet).iterator();

        List<PositionalProteinSignature> factsToAdd = Arrays.asList(positionalProteinSignature_1);

        List<Fact> mergedFacts = factMerger.merge(factSetIterator, factsToAdd);

        assertThat(mergedFacts.size(), is(5));
        assertThat(mergedFacts, containsInAnyOrder(
                protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2, positionalProteinSignature_1
        ));
    }

    @Test
    public void twoProteinsWithSameOrganismWithAlignments() {
        FactSet factSet = new FactSet();
        factSet.setFact(Arrays.asList(
                protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2,
                protein_2, organism_1, proteinSignature_2_1, proteinSignature_2_2
        ));
        Iterator<FactSet> factSetIterator = Arrays.asList(factSet).iterator();

        List<PositionalProteinSignature> factsToAdd = Arrays.asList(positionalProteinSignature_1);

        List<Fact> mergedFacts = factMerger.merge(factSetIterator, factsToAdd);

        assertThat(mergedFacts.size(), is(8));
        assertThat(mergedFacts, containsInAnyOrder(
                protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2, positionalProteinSignature_1,
                protein_2, proteinSignature_2_1, proteinSignature_2_2
        ));
    }

    @Test
    public void twoProteinsWithDifferentOrganismWithAlignments() {
        FactSet factSet = new FactSet();
        factSet.setFact(Arrays.asList(
                protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2,
                protein_3, organism_2, proteinSignature_3_1, proteinSignature_3_2
        ));
        Iterator<FactSet> factSetIterator = Arrays.asList(factSet).iterator();

        List<PositionalProteinSignature> factsToAdd = Arrays.asList(positionalProteinSignature_1,
                positionalProteinSignature_2);

        List<Fact> mergedFacts = factMerger.merge(factSetIterator, factsToAdd);

        assertThat(mergedFacts.size(), is(10));
        assertThat(mergedFacts, containsInAnyOrder(
                protein_1, organism_1, proteinSignature_1_1, proteinSignature_1_2, positionalProteinSignature_1,
                protein_3, organism_2, proteinSignature_3_1, proteinSignature_3_2, positionalProteinSignature_2
        ));
    }

    private static PositionalProteinSignature createPositionalProteinSignature(Protein protein, SignatureType type,
            String value,
            int frequency, int start, int end, String alignment) {
        PositionalProteinSignature positionalProteinSignature = new PositionalProteinSignature();
        positionalProteinSignature.setProtein(protein);
        Signature signature = new Signature();
        signature.setType(type);
        signature.setValue(value);
        positionalProteinSignature.setSignature(signature);
        positionalProteinSignature.setFrequency(frequency);

        positionalProteinSignature.setPositionStart(start);
        positionalProteinSignature.setPositionEnd(end);
        SequenceAlignment sequenceAlignment = new SequenceAlignment();
        sequenceAlignment.setValue(alignment);
        positionalProteinSignature.setAlignment(sequenceAlignment);

        return positionalProteinSignature;
    }

    private static ProteinSignature createProteinSignature(Protein protein, SignatureType type, String value, int frequency) {
        ProteinSignature proteinSignature = new ProteinSignature();
        proteinSignature.setProtein(protein);
        Signature signature = new Signature();
        signature.setType(type);
        signature.setValue(value);
        proteinSignature.setSignature(signature);
        proteinSignature.setFrequency(frequency);

        return proteinSignature;
    }

    private static Protein createProtein(String id, Organism organism, String sequence, String oln) {
        Protein protein = new Protein();
        protein.setId(id);
        protein.setOrganism(organism);
        GeneInformation geneInformation = createGeneInformation(oln);
        protein.setGene(geneInformation);
        ProteinSequence proteinSequence = createProteinSequence(sequence, false);
        protein.setSequence(proteinSequence);

        return protein;
    }

    private static GeneInformation createGeneInformation(String oln) {
        GeneInformation geneInformation = new GeneInformation();
        geneInformation.setOrfOrOlnNames(Arrays.asList(oln));
        return geneInformation;
    }

    private static ProteinSequence createProteinSequence(String sequence, boolean isFragment) {
        ProteinSequence proteinSequence = new ProteinSequence();
        proteinSequence.setValue(sequence);
        proteinSequence.setLength(sequence.length());
        proteinSequence.setIsFragment(isFragment);
        return proteinSequence;
    }

    private static Organism createOrganism(String id, String scientificName, List<Integer> taxIds) {
        Organism organism = new Organism();
        organism.setId(id);
        organism.setScientificName(scientificName);
        Lineage lineage = new Lineage();
        lineage.setIds(taxIds);
        organism.setLineage(lineage);

        return organism;
    }
}
