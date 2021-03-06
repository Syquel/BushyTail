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

package de.syquel.bushytail;

import de.syquel.bushytail.controller.IBushyTailController;
import de.syquel.bushytail.exception.BushyTailException;
import de.syquel.bushytail.factory.OlingoMetadataFactory;
import de.syquel.bushytail.factory.exception.OlingoMetadataFactoryException;
import org.apache.commons.lang3.ClassUtils;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for a new {@link BushyTail} instance.
 *
 * @author Frederik Boster
 * @author Clemens Bartz
 * @since 1.0
 */
public class BushyTailBuilder {

    /**
     * Maps JPA {@link javax.persistence.Entity} with their corresponding CRUD business controller.
     */
    private final Map<Class<?>, IBushyTailController<?>> entityControllerMap = new HashMap<Class<?>, IBushyTailController<?>>();

    /**
     * Maps OData {@link org.apache.olingo.commons.api.edm.provider.CsdlEntitySet} to JPA {@link javax.persistence.Entity}.
     */
    private final Map<FullQualifiedName, Class<?>> entityTypeMap = new HashMap<FullQualifiedName, Class<?>>();

    /**
     * Add a JPA entity and associate it with a business controller.
     * Determines the namespace and entity name automatically.
     *
     * @param <T> The class type of the JPA entity.
     * @param entityClass The class type of the JPA entity.
     * @param entityController The business controller which shall be associated to the JPA entity class to handle CRUDQ operations.
     */
    public <T> void addEntity(Class<T> entityClass, IBushyTailController<T> entityController) {
        String namespace = ClassUtils.getPackageName(entityClass);
        String entityName = entityClass.getSimpleName();

        addEntity(entityClass, entityName, namespace, entityController);
    }

    /**
     * Add a JPA entity and associate it with a business controller.
     *
     * @param <T> The class type of the JPA entity.
     * @param entityClass The class type of the JPA entity.
     * @param entityName The OData resource name for this entity class.
     * @param namespace The namespace of this OData resource.
     * @param entityController The business controller which shall be associated to the JPA entity class to handle CRUDQ operations.
     */
    public <T> void addEntity(Class<T> entityClass, String entityName, String namespace, IBushyTailController<T> entityController) {
        FullQualifiedName entityFQN = new FullQualifiedName(namespace, entityName);

        addEntity(entityClass, entityFQN, entityController);
    }

    /**
     * Add a JPA entity and associate it with a business controller.
     *
     * @param <T> The class type of the JPA entity.
     * @param entityClass The class type of the JPA entity.
     * @param entityFQN The unique full qualified name of this Odata resource.
     * @param entityController The business controller which shall be associated to the JPA entity class to handle CRUDQ operations.
     */
    public <T> void addEntity(Class<T> entityClass, FullQualifiedName entityFQN, IBushyTailController<T> entityController) {
        entityControllerMap.put(entityClass, entityController);
        entityTypeMap.put(entityFQN, entityClass);
    }

    /**
     * Build the odata metadata and other internal helper classes from the previously added JPA entity types via {@link #addEntity(Class, IBushyTailController)}.
     *
     * This method makes heavy use of reflection. Thus it is advised to execute this method only once in the lifetime of the application.
     */
    public BushyTail build() throws BushyTailException {
        final OlingoMetadataFactory metadataFactory = new OlingoMetadataFactory();

        for (Map.Entry<FullQualifiedName, Class<?>> entity : entityTypeMap.entrySet()) {
            final FullQualifiedName entityFQN = entity.getKey();
            final Class<?> entityType = entity.getValue();

            metadataFactory.addEntity(entityType, entityFQN);
        }

        final List<CsdlSchema> odataSchemas;
        try {
            odataSchemas = metadataFactory.createSchema("");
        } catch (OlingoMetadataFactoryException e) {
            throw new BushyTailException("Cannot build Olingo metadata", e);
        }

        return new BushyTail(odataSchemas, entityControllerMap, entityTypeMap);
    }

}
