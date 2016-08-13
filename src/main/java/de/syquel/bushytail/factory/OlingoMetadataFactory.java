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

package de.syquel.bushytail.factory;

import org.apache.commons.lang.ClassUtils;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Factory Class to generate Olingo OData {@link CsdlSchema} out of a collection of {@code JPA} {@link Entity}.
 *
 * @author Frederik Boster
 */
public class OlingoMetadataFactory {


    /**
     * Contains the mapping for basic Java types zo basic OData types.
     */
    private static final Map<Class<?>, FullQualifiedName> JAVA_TO_ODATA_TYPE_MAP = new HashMap<Class<?>, FullQualifiedName>();
    static {
        JAVA_TO_ODATA_TYPE_MAP.put(String.class, EdmPrimitiveTypeKind.String.getFullQualifiedName());
        JAVA_TO_ODATA_TYPE_MAP.put(Integer.class, EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        JAVA_TO_ODATA_TYPE_MAP.put(Long.class, EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
        JAVA_TO_ODATA_TYPE_MAP.put(Date.class, EdmPrimitiveTypeKind.Date.getFullQualifiedName());
        JAVA_TO_ODATA_TYPE_MAP.put(Double.class, EdmPrimitiveTypeKind.Double.getFullQualifiedName());
        JAVA_TO_ODATA_TYPE_MAP.put(Boolean.class, EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
        JAVA_TO_ODATA_TYPE_MAP.put(Byte.class, EdmPrimitiveTypeKind.Stream.getFullQualifiedName());
    }

    /**
     * Contains all JPA {@link Entity} within their respective namespace.
     */
    private final Map<String, Map<Class<?>, FullQualifiedName>> namespaceEntities = new HashMap<String, Map<Class<?>, FullQualifiedName>>();


    /**
     * Constructs {@link OlingoMetadataFactory}.
     */
    public OlingoMetadataFactory() {

    }

    /**
     * Adds a JPA {@link Entity} with their namespace to the factory context and
     * queues it for further processing.
     *
     * @param types A collection of JPA {@link Entity} which shall be included in the OData {@link CsdlSchema}.
     */
    public void addEntity(final Iterable<Class<?>> types) {
        for (final Class<?> type : types) {
            addEntity(type);
        }
    }

    /**
     * Adds a JPA {@link Entity} with their namespace to the factory context and
     * queues it for further processing.
     * The namespace and name of the entity are automatically calculated.
     *
     * @param type A JPA {@link Entity} which shall be included in the OData {@link CsdlSchema}.
     */
    public void addEntity(final Class<?> type) {
        String namespace = ClassUtils.getPackageName(type);
        String entityName = type.getSimpleName();

        FullQualifiedName entityFQN = new FullQualifiedName(namespace, entityName);

        addEntity(entityFQN, type);
    }


    /**
     * Adds a JPA {@link Entity} with their namespace to the factory context and
     * queues it for further processing.
     *
     * @param entityFQN The Full Qualified Name of the OData entity.
     * @param type A JPA {@link Entity} which shall be included in the OData {@link CsdlSchema}.
     */
    public void addEntity(final FullQualifiedName entityFQN, final Class<?> type) {
        Map<Class<?>, FullQualifiedName> entities = namespaceEntities.get(entityFQN.getNamespace());
        if (entities == null) {
            entities = new HashMap<Class<?>, FullQualifiedName>();
            namespaceEntities.put(entityFQN.getNamespace(), entities);
        }

        entities.put(type, entityFQN);
    }

    /**
     * Creates a list of OData {@link CsdlSchema} out of all queued JPA {@link Entity}.
     *
     * @param name The name of the schema.
     * @return The List of OData {@link CsdlSchema} which were generated out of the queued JPA {@link Entity}.
     *
     * @see #addEntity(Class)
     * @see #addEntity(Iterable)
     */
    public List<CsdlSchema> createSchema(final String name) {
        final List<CsdlSchema> schemas = new ArrayList<CsdlSchema>(namespaceEntities.size());

        // Loop through all Namespaces which were set
        for (final Map.Entry<String, Map<Class<?>, FullQualifiedName>> namespaceEntityEntry : namespaceEntities.entrySet()) {
            final String namespace = namespaceEntityEntry.getKey();
            final Map<Class<?>, FullQualifiedName> namespaceEntityMap = namespaceEntityEntry.getValue();

            // Loop through all JPA Entites which were set and create EntityTypes and EntitySets
            final List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
            final List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
            for (final Map.Entry<Class<?>, FullQualifiedName> entityEntry : namespaceEntityMap.entrySet()){
                final FullQualifiedName entityFQN = entityEntry.getValue();
                final Class<?> entity = entityEntry.getKey();

                final ODataEntityPair entityPair = createEntity(entity, entityFQN);

                entityTypes.add(entityPair.getEntityType());
                entitySets.add(entityPair.getEntitySet());
            }

            // Build EntityContainer
            final CsdlEntityContainer entityContainer = new CsdlEntityContainer();
            entityContainer.setName(name);
            entityContainer.setEntitySets(entitySets);

            // Build Schema
            final CsdlSchema schema = new CsdlSchema();
            schema.setNamespace(namespace);
            schema.setEntityTypes(entityTypes);
            schema.setEntityContainer(entityContainer);

            schemas.add(schema);
        }

        return schemas;
    }

    /**
     * Creates OData {@link CsdlEntityType} and {@link CsdlEntitySet} out of a JPA {@link Entity}.
     *
     * @param type The JPA {@link Entity}.
     * @param entityFQN The Full Qualified Name of the OData {@link CsdlEntitySet}.
     * @return The pair of generated OData {@link CsdlEntityType} and {@link CsdlEntitySet}.
     */
    private ODataEntityPair createEntity(final Class<?> type, final FullQualifiedName entityFQN) {
        final List<CsdlProperty> properties = new ArrayList<CsdlProperty>();
        final List<CsdlPropertyRef> primaryKeyProperties = new ArrayList<CsdlPropertyRef>();
        final List<CsdlNavigationProperty> navigationProperties = new ArrayList<CsdlNavigationProperty>();
        final List<CsdlNavigationPropertyBinding> navigationPropertyBindings = new ArrayList<CsdlNavigationPropertyBinding>();

        // Loop over fields of current Entity Class
        final Field[] typeFields = type.getDeclaredFields();
        for (final Field typeField : typeFields) {
            processProperty(typeField, properties, primaryKeyProperties, navigationProperties, navigationPropertyBindings);
        }

        // Build EntityType
        final CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(type.getSimpleName());
        entityType.setProperties(properties);
        entityType.setKey(primaryKeyProperties);
        entityType.setNavigationProperties(navigationProperties);

        // Build EntitySet
        final CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(getJPAEntityName(type));
        entitySet.setType(entityFQN);
        entitySet.setNavigationPropertyBindings(navigationPropertyBindings);

        return new ODataEntityPair(entitySet, entityType);
    }

    /**
     * Processes a {@link Field} out of a JPA {@link Entity} and generates the necessary OData objects.
     *
     * @param typeField A field of a JPA {@link Entity}.
     * @param properties A collection of OData properties which will be filled by this function
     *                   and can be used in {@link CsdlEntityType#setProperties(List)}.
     * @param primaryKeyProperties A collection of OData primary keys which will be filled by this function
     *                             and can be used in {@link CsdlEntityType#setKey(List)}.
     * @param navigationProperties A collection of OData navigation properties which will be filled by this function
     *                             and can be used in {@link CsdlEntityType#setNavigationProperties(List)}.
     * @param navigationPropertyBindings A collection of OData navigation property bindings which will be filled by this function
     *                                   and can be used in {@link CsdlEntitySet#setNavigationPropertyBindings(List)}.
     */
    private void processProperty(final Field typeField, final Collection<CsdlProperty> properties, final Collection<CsdlPropertyRef> primaryKeyProperties,
                                 final Collection<CsdlNavigationProperty> navigationProperties, final Collection<CsdlNavigationPropertyBinding> navigationPropertyBindings) {
        final String propertyName = typeField.getName();

        // Determine if field is collection or Enumeration and assign actual type
        final Class<?> propertyType;
        final Boolean isCollection;
        if (typeField.isAnnotationPresent(Enumerated.class)) {
            final Enumerated enumAnnotation = typeField.getAnnotation(Enumerated.class);

            isCollection = Collection.class.isAssignableFrom(typeField.getType());

            if (enumAnnotation.value() == EnumType.ORDINAL) {
                propertyType = Integer.class;
            } else if (enumAnnotation.value() == EnumType.STRING) {
                propertyType = String.class;
            } else {
                throw new RuntimeException("Could not determine JPA type for class '" + typeField.getType().getName() + "': Not a valid Enumeration!");
            }

        } else if (Collection.class.isAssignableFrom(typeField.getType())) {
            propertyType = (Class<?>) ((ParameterizedType)typeField.getGenericType()).getActualTypeArguments()[0];
            isCollection = true;
        } else {
            propertyType = typeField.getType();
            isCollection = false;
        }

        // Determine OData type or set JPA entity
        final FullQualifiedName odataType = getODataType(propertyType);

        // Sync JPA Column attributes with OData property
        final Column columnAnnotation = typeField.getAnnotation(Column.class);
        final Boolean isNullable = (columnAnnotation != null) && columnAnnotation.nullable();

        // Determine Mapping Partner for Navigation Path
        final String mappingPartner = getMappingPartner(typeField);
        if (mappingPartner == null) {
            // Define property for entity
            final CsdlProperty property = new CsdlProperty();
            property.setName(propertyName);
            property.setType(odataType);
            property.setCollection(isCollection);
            property.setNullable(isNullable);

            properties.add(property);
        } else {
            // Define navigation paths between entities
            final CsdlNavigationProperty navProperty = new CsdlNavigationProperty();
            navProperty.setName(propertyName);
            navProperty.setType(odataType);
            navProperty.setCollection(isCollection);
            navProperty.setNullable(isNullable);
            navProperty.setPartner(mappingPartner);

            navigationProperties.add(navProperty);

            // Define navigation paths between EntitySets
            final CsdlNavigationPropertyBinding navigationPropertyBinding = new CsdlNavigationPropertyBinding();
            navigationPropertyBinding.setTarget(getJPAEntityName(propertyType));
            navigationPropertyBinding.setPath(mappingPartner);

            navigationPropertyBindings.add(navigationPropertyBinding);
        }

        // Build key-set for entity by JPA Id annotation
        if (typeField.isAnnotationPresent(Id.class)) {
            final CsdlPropertyRef primaryKeyProperty = new CsdlPropertyRef();
            primaryKeyProperty.setName(typeField.getName());

            primaryKeyProperties.add(primaryKeyProperty);
        }

    }

    /**
     * Determines the respective OData type of a class.
     * <p>
     * If possible the class is mapped to a basic OData type;
     * otherwise the respective OData entity is looked up.
     * </p>
     *
     * @param type The class the OData type gets determined for.
     * @return The basic OData type or an OData entity.
     *
     * @see #JAVA_TO_ODATA_TYPE_MAP
     */
    private FullQualifiedName getODataType(final Class<?> type) {
        FullQualifiedName odataType = JAVA_TO_ODATA_TYPE_MAP.get(type);

        if (odataType == null) {
            // Loop through all namespaces to lookup if propertyType is an JPA entity
            for (final Map.Entry<String, Map<Class<?>, FullQualifiedName>> namespaceEntityEntry : namespaceEntities.entrySet()) {

                final String entityNamespace = namespaceEntityEntry.getKey();
                final Map<Class<?>, FullQualifiedName> namespaceEntityMap = namespaceEntityEntry.getValue();
                if (namespaceEntityMap != null) {
                    FullQualifiedName entityFQN = namespaceEntityMap.get(type);

                    if (entityFQN != null) {
                        odataType = entityFQN;
                    }

                    break;
                }
            }

            if (odataType == null) {
                throw new RuntimeException("Could not determine OData type for class '" + type.getName() + "'");
            }
        }

        return odataType;
    }

    /**
     * Determines the table name of a JPA {@link Entity}.
     *
     * @param type The JPA {@link Entity}.
     * @return The determined table name.
     */
    private static String getJPAEntityName(final Class<?> type) {
        // Determine JPA Entity name
        final Table typeTable = type.getAnnotation(Table.class);
        if (typeTable == null || typeTable.name() == null || typeTable.name().isEmpty()) {
            throw new RuntimeException("Could not determine EntitySet name: @Table annotation is missing! (" + type.getName() + ")");
        }

        return typeTable.name();
    }

    /**
     * Determines the name of the partner-property of a mapped property in a JPA {@link Entity}.
     *
     * @param typeField The field of a JPA {@link Entity}.
     * @return The determined name of the partner-property.
     */
    private static String getMappingPartner(final Field typeField) {
        String mappingPartner = null;

        if (typeField.isAnnotationPresent(ManyToOne.class)) {
            final Field[] mappingFields = typeField.getType().getDeclaredFields();
            for (final Field mappingField : mappingFields) {
                final OneToMany mappingAnnotation = mappingField.getAnnotation(OneToMany.class);

                if (mappingAnnotation != null && typeField.getName().equals(mappingAnnotation.mappedBy())) {
                    mappingPartner = mappingField.getName();
                    break;
                }
            }
        } else if (typeField.isAnnotationPresent(OneToMany.class)) {
            final OneToMany mapping = typeField.getAnnotation(OneToMany.class);
            mappingPartner = mapping.mappedBy();
        } else if (typeField.isAnnotationPresent(OneToOne.class)) {
            final OneToOne mapping = typeField.getAnnotation(OneToOne.class);
            mappingPartner = mapping.mappedBy();
        } else if (typeField.isAnnotationPresent(ManyToMany.class)) {
            final ManyToMany mapping = typeField.getAnnotation(ManyToMany.class);
            mappingPartner = mapping.mappedBy();
        }

        return mappingPartner;
    }


    /**
     * Data Class to hold an EntitySet and EntityType Pair.
     */
    private static class ODataEntityPair {

        private final CsdlEntitySet entitySet;
        private final CsdlEntityType entityType;

        public ODataEntityPair(final CsdlEntitySet entitySet, final CsdlEntityType entityType) {
            this.entitySet = entitySet;
            this.entityType = entityType;
        }

        public CsdlEntitySet getEntitySet() {
            return entitySet;
        }

        public CsdlEntityType getEntityType() {
            return entityType;
        }

    }


}
