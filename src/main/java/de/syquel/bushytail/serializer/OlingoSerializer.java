/*
 * Copyright 2016, Frederik Boster
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.syquel.bushytail.serializer;

import de.syquel.bushytail.serializer.exception.OlingoSerializerException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Converts Java objects to Olingo entities.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 */
public final class OlingoSerializer {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(OlingoSerializer.class);

    /**
     * Hidden constructor.
     */
    private OlingoSerializer() {

    }

    /**
     * Convert an {@link T object} to an {@link Entity Olingo entity}.
     * @param entityType the type of the entity
     * @param entityObject the object to convert
     * @param <T> the type of the entity
     * @return an Olingo entity
     * @throws OlingoSerializerException if the method cannot access a property
     */
    public static <T> Entity serialize(EdmEntityType entityType, T entityObject) throws OlingoSerializerException {
        final Entity olingoEntity = new Entity();
        olingoEntity.setType(entityType.getFullQualifiedName().getFullQualifiedNameAsString());

        for (String propertyName : entityType.getPropertyNames()) {
            try {
                final Method propertyAccessor = PropertyUtils.getPropertyDescriptor(entityObject, propertyName).getReadMethod();
                final Object value = propertyAccessor.invoke(entityObject);

                olingoEntity.addProperty(new Property(null, propertyName, ValueType.PRIMITIVE, value));
            } catch (Exception e) {
                logger.error("Cannot access property '" + propertyName + "' of class '" + entityObject.getClass() + "'", e);
                throw new OlingoSerializerException("Cannot access property '" + propertyName + "' of class '" + entityObject.getClass() + "'", e);
            }
        }

        return olingoEntity;
    }

}
