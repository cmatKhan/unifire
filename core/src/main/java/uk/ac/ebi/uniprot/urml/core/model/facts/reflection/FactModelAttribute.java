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

import org.uniprot.urml.facts.Fact;

/**
 * Represents the attribute of a {@link Fact} class, providing information about the type and name of the attribute.
 *
 * @author Alexandre Renaux
 */
public class FactModelAttribute {

    private final Class<?> attributeType;
    private final String name;
    private final boolean isCollection;

    public FactModelAttribute(Class<?> attributeType, String name, boolean isCollection) {
        this.attributeType = attributeType;
        this.name = name;
        this.isCollection = isCollection;
    }

    public Class<?> getAttributeType() {
        return attributeType;
    }

    public String getName() {
        return name;
    }

    public boolean isRelational(){
        return Fact.class.isAssignableFrom(this.attributeType);
    }

    public boolean isCollection() {
        return isCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FactModelAttribute that = (FactModelAttribute) o;

        return attributeType.equals(that.attributeType) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = attributeType.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FactAttribute{" + "attributeType=" + attributeType + ", attribute='" + name + '\'' + '}';
    }


}
