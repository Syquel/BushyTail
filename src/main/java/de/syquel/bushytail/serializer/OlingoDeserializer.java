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

import java.lang.reflect.Method;

/**
 * Converts Olingo entities to Java objects.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 */
public class OlingoDeserializer {

    public static <T> T deserialize(Class<T> entityClass, Entity olingoEntity) throws OlingoDeserializerException {
        T entity;
        try {
            entity = entityClass.newInstance();
        } catch (Exception e) {
            throw new OlingoDeserializerException("Cannot instantiate entity for class '" + entityClass.getName() + "'", e);
        }


        for (Property property : olingoEntity.getProperties()) {
            try {
                Method propertyAccessor = PropertyUtils.getPropertyDescriptor(entity, property.getName()).getWriteMethod();

                propertyAccessor.invoke(entity, property);
            } catch (Exception e) {
                throw new OlingoDeserializerException("Cannot set property '" + property.getName() + "' of class '" + entityClass.getName() + "'", e);
            }
        }


        return entity;
    }

}
