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

package uk.ac.ebi.uniprot.urml.input.collections;

import java.util.Collection;
import java.util.Iterator;
import org.uniprot.urml.facts.FactSet;

/**
 * Repartition FactSet data into partitions of given size
 *
 * @author Alexandre Renaux
 */
public class FactSetPartitioner implements Iterator<FactSetPartitioner.Partition> {

    private final Iterator<FactSet> sourceFactSetIterator;
    private final int maxFactSets;

    public FactSetPartitioner(Iterator<FactSet> sourceFactSetIterator, Integer partitionSize){
        this.sourceFactSetIterator = sourceFactSetIterator;
        this.maxFactSets = partitionSize;
    }

    public FactSetPartitioner(Collection<FactSet> sourceFactSets, Integer partitionSize) {
        this(sourceFactSets.iterator(), partitionSize);
    }

    @Override
    public boolean hasNext() {
        return sourceFactSetIterator.hasNext();
    }

    @Override
    public FactSetPartitioner.Partition next() {
        int counter = 0;
        FactSet factSet = new FactSet();
        while (sourceFactSetIterator.hasNext() && counter < maxFactSets){
            sourceFactSetIterator.next().getFact().forEach(fact -> factSet.getFact().add(fact));
            counter++;
        }
        return new Partition(factSet, counter);
    }

    public class Partition {

        private final FactSet factSet;
        private final int size;

        public Partition(FactSet factSet, int size) {
            this.factSet = factSet;
            this.size = size;
        }

        public FactSet getFactSet() {
            return factSet;
        }

        public int getSize() {
            return size;
        }
    }


}
