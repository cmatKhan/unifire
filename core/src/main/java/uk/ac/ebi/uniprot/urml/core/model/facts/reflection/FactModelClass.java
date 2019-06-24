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

package uk.ac.ebi.uniprot.urml.core.model.facts.reflection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.uniprot.urml.facts.Fact;

/**
 * Provides information about the class of a {@link Fact} and all its attributes.
 *
 * @author Alexandre Renaux
 */
public class FactModelClass {

    private final Class<?> javaType;
    private final String namespace;
    private final Map<String, FactModelAttribute> factAttributes;
    private final Set<FactModelAttribute> relationalAttributes;

    public FactModelClass(Class<?> factClass, String namespace, Map<String, FactModelAttribute> factAttributes) {
        this.javaType = factClass;
        this.namespace = namespace;
        this.factAttributes = factAttributes;
        this.relationalAttributes = factAttributes.values().stream()
                .filter(FactModelAttribute::isRelational)
                .collect(Collectors.toSet());
    }

    public Class<?> getJavaType(){
        return javaType;
    }

    public String getName() {
        return javaType.getSimpleName();
    }

    public String getNamespace(){
        return namespace;
    }

    public Collection<FactModelAttribute> getRelationalAttributes(){
        return relationalAttributes;
    }

    public Collection<FactModelAttribute> getAttributes(){
        return factAttributes.values();
    }

    public FactModelAttribute getAttribute(String attributeName) {
        if (factAttributes.containsKey(attributeName)){
            return factAttributes.get(attributeName);
        } else {
            throw new FactModelReflectionException(attributeName+" is not an attribute of "+javaType);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FactModelClass factClass1 = (FactModelClass) o;

        return javaType.equals(factClass1.javaType);
    }

    @Override
    public int hashCode() {
        return javaType.hashCode();
    }

    @Override
    public String toString() {
        return "FactClass{" + "factClass=" + javaType + ", namespace='" + namespace + '\'' + ", factAttributes=" +
                factAttributes + ", relationalAttributes=" + relationalAttributes + '}';
    }
}
