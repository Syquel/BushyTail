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

package de.syquel.bushytail.service.subprocessor;

import de.syquel.bushytail.controller.IBushyTailController;
import de.syquel.bushytail.serializer.OlingoSerializer;
import de.syquel.bushytail.serializer.exception.OlingoSerializerException;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Processes CRUD operations on a single Olingo entity.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 */
public class BushyTailEntitySetSubProcessor {

    private final Map<FullQualifiedName, Class<?>> entityTypeMap;
    private final Map<Class<?>, IBushyTailController<?>> entityProcessorMap;

    public BushyTailEntitySetSubProcessor(final Map<FullQualifiedName, Class<?>> entityTypeMap, final Map<Class<?>, IBushyTailController<?>> entityProcessorMap) {
        this.entityTypeMap = entityTypeMap;
        this.entityProcessorMap = entityProcessorMap;
    }

    public Object read(UriInfo uriInfo) throws ODataApplicationException {
        // TODO consider UriInfo Options: Apply, Count, Expand, Filter, Id, Search, Select, Skip, Top, OrderBy, SkipToken
        List<UriResource> uriResources = uriInfo.getUriResourceParts();

        for (UriResource uriResource : uriResources) {
            switch (uriResource.getKind()) {
                case entitySet: {
                    UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriResource;

                    Class<?> entityClass = entityTypeMap.get(resourceEntitySet.getEntityType().getFullQualifiedName());
                    IBushyTailController<?> entityController = entityProcessorMap.get(entityClass);

                    List<UriParameter> keyPredicates = resourceEntitySet.getKeyPredicates();
                    Object entity = entityController.read(keyPredicates);

                    try {
                        Entity olingoEntity = OlingoSerializer.serialize(resourceEntitySet.getEntityType(), entity);
                    } catch (OlingoSerializerException e) {
                        throw new ODataApplicationException("Cannot serialize Olingo entity '" + resourceEntitySet.getType().getName() + "'", 500, Locale.ENGLISH, e);
                    }

                    break;
                }
                case navigationProperty: {
                    UriResourceNavigation resourceNavigation = (UriResourceNavigation) uriResource;
                    break;
                }
                case primitiveProperty: {
                    UriResourcePrimitiveProperty resourcePrimitiveProperty = (UriResourcePrimitiveProperty) uriResource;
                    break;
                }
                case complexProperty: {
                    UriResourceComplexProperty resourceComplexProperty = (UriResourceComplexProperty) uriResource;
                    break;
                }
                case value: {
                    UriResourceValue resourceValue = (UriResourceValue) uriResource;
                    break;
                }
                case count: {
                    UriResourceCount resourceCount = (UriResourceCount) uriResource;
                    break;
                }
                case action: {
                    UriResourceAction resourceAction = (UriResourceAction) uriResource;
                    break;
                }
                case function: {
                    UriResourceFunction resourceFunction = (UriResourceFunction) uriResource;
                    break;
                }
                case ref: {
                    UriResourceRef resourceRef = (UriResourceRef) uriResource;
                    break;
                }
                case it: {
                    UriResourceIt resourceIt = (UriResourceIt) uriResource;
                    break;
                }
                case root: {
                    UriResourceRoot resourceRoot = (UriResourceRoot) uriResource;
                    break;
                }
                case singleton: {
                    UriResourceSingleton resourceSingleton = (UriResourceSingleton) uriResource;
                    break;
                }
                case lambdaAll: {
                    UriResourceLambdaAll resourceLambdaAll = (UriResourceLambdaAll) uriResource;
                    break;
                }
                case lambdaAny: {
                    UriResourceLambdaAny resourceLambdaAny = (UriResourceLambdaAny) uriResource;
                    break;
                }
                case lambdaVariable: {
                    UriResourceLambdaVariable resourceLambdaVariable = (UriResourceLambdaVariable) uriResource;
                    break;
                }
                default:
                    break;
            }
        }

        // TODO
        return null;
    }

}
