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
