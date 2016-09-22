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

import de.syquel.bushytail.controller.IBushyTailController;
import de.syquel.bushytail.serializer.OlingoDeserializer;
import de.syquel.bushytail.serializer.exception.OlingoDeserializerException;
import de.syquel.bushytail.service.subprocessor.BushyTailEntitySetSubProcessor;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * Processes CRUD operations on a single Olingo entity.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 */
public class BushyTailEntityProcessor implements EntityProcessor {

    private final Map<FullQualifiedName, Class<?>> entityTypeMap;
    private final Map<Class<?>, IBushyTailController<?>> entityProcessorMap;

    private final BushyTailEntitySetSubProcessor entitySetSubProcessor;

    /** The odata object. */
    private OData oData = null;

    /** The service metadata. */
    private ServiceMetadata serviceMetadata = null;

    public BushyTailEntityProcessor(final Map<FullQualifiedName, Class<?>> entityTypeMap, final Map<Class<?>, IBushyTailController<?>> entityProcessorMap) {
        this.entityTypeMap = entityTypeMap;
        this.entityProcessorMap = entityProcessorMap;

        entitySetSubProcessor = new BushyTailEntitySetSubProcessor(entityTypeMap, entityProcessorMap);
    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.oData = oData;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType responseContentType) throws ODataApplicationException, ODataLibraryException {
        entitySetSubProcessor.read(uriInfo);
    }

    @Override
    public void createEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {
        createEntityInternal(oDataRequest, oDataResponse, uriInfo, contentType, contentType1);
    }

    public <T> void createEntityInternal(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType requestContentType, ContentType responseContentType) throws ODataApplicationException, ODataLibraryException {
        UriResourceEntitySet uriEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        Class<T> entityClass = (Class<T>) entityTypeMap.get(uriEntitySet.getEntityType().getFullQualifiedName());
        EdmEntityType edmEntityType = serviceMetadata.getEdm().getEntityType(uriEntitySet.getEntityType().getFullQualifiedName());

        InputStream requestInputStream = oDataRequest.getBody();
        ODataDeserializer deserializer = this.oData.createDeserializer(requestContentType);
        DeserializerResult deserializerResult = deserializer.entity(requestInputStream, edmEntityType);
        Entity olingoEntity = deserializerResult.getEntity();

        T entity = null;
        try {
            entity = OlingoDeserializer.deserialize(entityClass, olingoEntity);
        } catch (OlingoDeserializerException e) {
            throw new ODataApplicationException("Cannot deserialize Olingo entity '" + olingoEntity.getType() + "'", 500, Locale.ENGLISH, e);
        }

        IBushyTailController<T> controller = (IBushyTailController<T>) entityProcessorMap.get(entityClass);
        controller.create(entity);
    }

    @Override
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType requestContentType, ContentType responseContentType) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

    }

}
