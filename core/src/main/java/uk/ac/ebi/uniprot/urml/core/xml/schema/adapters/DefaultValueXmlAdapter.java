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

package uk.ac.ebi.uniprot.urml.core.xml.schema.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link XmlAdapter} implementation which marshalls the annotated
 * attribute having the specified defaultValue into NULL (i.e omitted by the marshaller).
 * Usage: {@link #getDefaultValue()} should be implemented to specify the value to nullify
 * (value is compared by its {@link T#equals(Object)} method), and the XML entity class
 * should have its attribute annotated with the implementation to transform on marshalling.
 *
 * @author Alexandre Renaux
 */
public abstract class DefaultValueXmlAdapter<T> extends XmlAdapter<T, T> {

    public T getDefaultValue() { return null; }

    public T unmarshal(T value) throws Exception {
        return value;
    }

    public T marshal(T value) throws Exception {
        return (value == null) || value.equals(getDefaultValue()) ? null : value;
    }
}

