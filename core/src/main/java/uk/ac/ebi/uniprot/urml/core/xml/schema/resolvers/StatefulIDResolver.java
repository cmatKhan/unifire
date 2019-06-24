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

package uk.ac.ebi.uniprot.urml.core.xml.schema.resolvers;

import uk.ac.ebi.uniprot.urml.core.UniFireRuntimeException;

import com.sun.xml.bind.IDResolver;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.xml.bind.ValidationEventHandler;
import org.xml.sax.SAXException;

/**
 * {@link IDResolver} implementation to keep track of id references (ID/IDREF) across multiple unmarshalling operations
 * (i.e {@link IDResolver#startDocument(ValidationEventHandler)}.
 * @see <a href=https://stackoverflow.com/questions/5319024/using-jaxb-to-cross-reference-xmlids-from-two-xml-files">https://stackoverflow.com/questions/5319024/</a>
 */
public final class StatefulIDResolver extends IDResolver {

    public static final class CallableImplementation implements Callable<Object> {
        private final Object value;

        private CallableImplementation(final Object value) {
            this.value = value;
        }

        @Override
        public Object call() {
            return value;
        }
    }

    private final Map<KeyAndClass, Object> m = new HashMap<>();

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized CallableImplementation resolve(final String key, final Class clazz) throws SAXException {
        if (key == null) {
            throw new UniFireRuntimeException("Got null as key input in StatefulIDResolver method resolve");
        }
        if (clazz == null) {
            throw new UniFireRuntimeException("Got null as clazz input in StatefulIDResolver method resolve");
        }

        final KeyAndClass keyAndClass = new KeyAndClass(clazz, key);
        final Object value = m.get(keyAndClass);
        return new CallableImplementation(value);
    }

    static class KeyAndClass {
        final Class<?> clazz;
        final String key;

        KeyAndClass(final Class<?> clazz, final String key) {
            this.clazz = clazz;
            this.key = key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + clazz.hashCode();
            result = prime * result + key.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final KeyAndClass other = (KeyAndClass) obj;
            return clazz.equals(other.clazz) && key.equals(other.key);
        }

    }

    @Override
    public synchronized void bind(final String key, final Object value) throws SAXException {
        if (key == null) {
            throw new UniFireRuntimeException("Got null as key input in StatefulIDResolver method bind");
        }
        if (value == null) {
            throw new UniFireRuntimeException("Got null as value input in StatefulIDResolver method bind");
        }
        Class<?> clazz = value.getClass();
        assert clazz != null;
        final KeyAndClass keyAndClass = new KeyAndClass(clazz, key);
        final Object oldValue = m.put(keyAndClass, value);
        if (oldValue != null) {
            final String message = MessageFormat.format("duplicated keyAndClass ''{0}'' => ''{1}'' - old: ''{2}''",
                    keyAndClass, value, oldValue);
            throw new AssertionError(message);
        }
    }
}