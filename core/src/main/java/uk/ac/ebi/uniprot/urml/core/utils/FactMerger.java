package uk.ac.ebi.uniprot.urml.core.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.uniprot.urml.facts.Fact;
import org.uniprot.urml.facts.FactSet;
import org.uniprot.urml.facts.PositionalProteinSignature;

/**
 * Created by Hermann Zellner on 21/07/2020.
 */
public class FactMerger {

    public List<Fact> merge(Iterator<FactSet> factSetIterator, List<PositionalProteinSignature> factsToAdd) {
        Iterable<FactSet> factSetIterable = () -> factSetIterator;
        Set<Fact> facts = StreamSupport.stream(factSetIterable.spliterator(), false)
                .flatMap(e -> e.getFact().stream())
                .collect(Collectors.toCollection(HashSet::new));
        facts.addAll(factsToAdd);

        return new ArrayList<>(facts);
    }
}
