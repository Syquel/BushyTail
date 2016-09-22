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

import java.util.List;

/**
 * Provides the service metadata, including Entity Data Model (EDM), the entities and their relationships.
 *
 * @author Clemens Bartz
 * @since 1.0
 */
public class BushyTailEdmProvider extends CsdlAbstractEdmProvider {

    List<CsdlSchema> odataSchemas;

    public BushyTailEdmProvider(List<CsdlSchema> odataSchemas) {
        this.odataSchemas = odataSchemas;
    }

    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) throws ODataException {
        return super.getEnumType(enumTypeName);
    }

    @Override
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName typeDefinitionName) throws ODataException {
        return super.getTypeDefinition(typeDefinitionName);
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
        return super.getEntityType(entityTypeName);
    }

    @Override
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
        return super.getComplexType(complexTypeName);
    }

    @Override
    public List<CsdlAction> getActions(FullQualifiedName actionName) throws ODataException {
        return super.getActions(actionName);
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName functionName) throws ODataException {
        return super.getFunctions(functionName);
    }

    @Override
    public CsdlTerm getTerm(FullQualifiedName termName) throws ODataException {
        return super.getTerm(termName);
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        return super.getEntitySet(entityContainer, entitySetName);
    }

    @Override
    public CsdlSingleton getSingleton(FullQualifiedName entityContainer, String singletonName) throws ODataException {
        return super.getSingleton(entityContainer, singletonName);
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) throws ODataException {
        return super.getActionImport(entityContainer, actionImportName);
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) throws ODataException {
        return super.getFunctionImport(entityContainer, functionImportName);
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        return super.getEntityContainerInfo(entityContainerName);
    }

    @Override
    public List<CsdlAliasInfo> getAliasInfos() throws ODataException {
        return super.getAliasInfos();
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        return super.getSchemas();
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        return super.getEntityContainer();
    }

    @Override
    public CsdlAnnotations getAnnotationsGroup(FullQualifiedName targetName, String qualifier) throws ODataException {
        return super.getAnnotationsGroup(targetName, qualifier);
    }

}
