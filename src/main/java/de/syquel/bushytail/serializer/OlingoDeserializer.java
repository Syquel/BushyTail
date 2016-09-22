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

import de.syquel.bushytail.serializer.exception.OlingoDeserializerException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Converts Olingo entities to Java objects.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 */
public final class OlingoDeserializer {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(OlingoDeserializer.class);

    /**
     * Hidden constructor.
     */
    private OlingoDeserializer() {

    }

    /**
     * Convert an {@link Entity Olingo entity} to an {@link T object}.
     * @param entityClass the class of the object
     * @param olingoEntity the olingo entity to convert
     * @param <T> the type of the object
     * @return the object
     * @throws OlingoDeserializerException if the class cannot be instantiated or a property cannot be set
     */
    public static <T> T deserialize(Class<T> entityClass, Entity olingoEntity) throws OlingoDeserializerException {
        T entity;
        try {
            entity = entityClass.newInstance();
        } catch (Exception e) {
            logger.error("Cannot instantiate entity for class '" + entityClass.getName() + "'", e);
            throw new OlingoDeserializerException("Cannot instantiate entity for class '" + entityClass.getName() + "'", e);
        }

        for (Property property : olingoEntity.getProperties()) {
            try {
                final Method propertyAccessor = PropertyUtils.getPropertyDescriptor(entity, property.getName()).getWriteMethod();

                propertyAccessor.invoke(entity, property);
            } catch (Exception e) {
                logger.error("Cannot set property '" + property.getName() + "' of class '" + entityClass.getName() + "'", e);
                throw new OlingoDeserializerException("Cannot set property '" + property.getName() + "' of class '" + entityClass.getName() + "'", e);
            }
        }

        return entity;
    }

}
