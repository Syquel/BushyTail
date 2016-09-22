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

package de.syquel.bushytail.service;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides the service metadata, including Entity Data Model (EDM), the entities and their relationships.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 */
public class BushyTailEdmProvider extends CsdlAbstractEdmProvider {

    private final Map<String, CsdlSchema> odataSchemas;

    public BushyTailEdmProvider(final Map<String, CsdlSchema> odataSchemas) {
        this.odataSchemas = odataSchemas;
    }

    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) throws ODataException {
        final CsdlSchema schema = getSchema(enumTypeName.getNamespace());

        return schema.getEnumType(enumTypeName.getName());
    }

    @Override
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName typeDefinitionName) throws ODataException {
        final CsdlSchema schema = getSchema(typeDefinitionName.getNamespace());

        return schema.getTypeDefinition(typeDefinitionName.getName());
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
        final CsdlSchema schema = getSchema(entityTypeName.getNamespace());

        return schema.getEntityType(entityTypeName.getName());
    }

    @Override
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
        final CsdlSchema schema = getSchema(complexTypeName.getNamespace());

        return schema.getComplexType(complexTypeName.getName());
    }

    @Override
    public List<CsdlAction> getActions(FullQualifiedName actionName) throws ODataException {
        final CsdlSchema schema = getSchema(actionName.getNamespace());

        return schema.getActions(actionName.getName());
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName functionName) throws ODataException {
        final CsdlSchema schema = getSchema(functionName.getNamespace());

        return schema.getFunctions(functionName.getName());
    }

    @Override
    public CsdlTerm getTerm(FullQualifiedName termName) throws ODataException {
        final CsdlSchema schema = getSchema(termName.getNamespace());

        return schema.getTerm(termName.getName());
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        final CsdlSchema schema = getSchema(entityContainer.getNamespace());

        return schema.getEntityContainer().getEntitySet(entitySetName);
    }

    @Override
    public CsdlSingleton getSingleton(FullQualifiedName entityContainer, String singletonName) throws ODataException {
        final CsdlSchema schema = getSchema(entityContainer.getNamespace());

        return schema.getEntityContainer().getSingleton(singletonName);
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) throws ODataException {
        final CsdlSchema schema = getSchema(entityContainer.getNamespace());

        return schema.getEntityContainer().getActionImport(actionImportName);
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) throws ODataException {
        final CsdlSchema schema = getSchema(entityContainer.getNamespace());

        return schema.getEntityContainer().getFunctionImport(functionImportName);
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        final CsdlSchema schema = getSchema(entityContainerName.getNamespace());
        CsdlEntityContainer entityContainer = schema.getEntityContainer();

        if (!entityContainer.getName().equals(entityContainerName.getName())) {
            return null;
        }

        final CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
        entityContainerInfo.setContainerName(new FullQualifiedName(schema.getNamespace(), entityContainer.getName()));
        entityContainerInfo.setExtendsContainer(entityContainer.getExtendsContainerFQN());

        return entityContainerInfo;
    }

    @Override
    public List<CsdlAliasInfo> getAliasInfos() throws ODataException {
        final List<CsdlAliasInfo> aliasInfos = new ArrayList<CsdlAliasInfo>();

        for (CsdlSchema schema : odataSchemas.values()) {
            final CsdlAliasInfo aliasInfo = new CsdlAliasInfo();
            aliasInfo.setAlias(schema.getAlias());
            aliasInfo.setNamespace(schema.getNamespace());

            aliasInfos.add(aliasInfo);
        }

        return aliasInfos;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        return new ArrayList<CsdlSchema>(odataSchemas.values());
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        // TODO
        return null;
    }

    @Override
    public CsdlAnnotations getAnnotationsGroup(FullQualifiedName targetName, String qualifier) throws ODataException {
        final CsdlSchema schema = getSchema(targetName.getNamespace());

        return schema.getAnnotationGroup(targetName.getName(), qualifier);
    }

    private CsdlSchema getSchema(String namespace) throws ODataException {
        final CsdlSchema schema = odataSchemas.get(namespace);
        if (schema == null) {
            throw new ODataException("No Olingo schema for namespace '" + namespace + "' defined");
        }

        return schema;
    }

}
