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
import de.syquel.bushytail.factory.OlingoMetadataFactory;
import de.syquel.bushytail.helper.BushyTailCSRFProtectionHelper;
import de.syquel.bushytail.service.BushyTailEdmProvider;
import de.syquel.bushytail.service.BushyTailEntityProcessor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * The BushyTail class should be used by the developer.
 * <p/>
 * It converts JPA entities and {@link de.syquel.bushytail.controller.IBushyTailController}s to an Olingo OData Version 4 service.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 */
public class BushyTail {

    private BushyTailCSRFProtectionHelper csrfProtectionHelper = new BushyTailCSRFProtectionHelper();

    private Map<Class<?>, IBushyTailController<?>> entityControllerMap = new HashMap<Class<?>, IBushyTailController<?>>();
    private Map<FullQualifiedName, Class<?>> entityTypeMap = new HashMap<FullQualifiedName, Class<?>>();
    private List<CsdlSchema> odataSchemas = new ArrayList<CsdlSchema>();

    /**
     * Serve a request from a Servlet.
     *
     * @param req The HTTP request as given by {@link javax.servlet.http.HttpServlet}.
     * @param resp The HTTP response as given by {@link javax.servlet.http.HttpServlet}, which will be modified.
     */
    public void service(final HttpServletRequest req, final HttpServletResponse resp) {
        csrfProtectionHelper.process(req, resp);

        final OData oData = OData.newInstance();
        final ServiceMetadata edm = oData.createServiceMetadata(new BushyTailEdmProvider(), new ArrayList<EdmxReference>(0));

        final ODataHttpHandler handler = oData.createHandler(edm);
        handler.register(new BushyTailEntityProcessor(entityTypeMap, entityControllerMap));

        handler.process(req, resp);
    }

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
    public void build() {
        OlingoMetadataFactory metadataFactory = new OlingoMetadataFactory();

        for (Map.Entry<FullQualifiedName, Class<?>> entity : entityTypeMap.entrySet()) {
            FullQualifiedName entityFQN = entity.getKey();
            Class<?> entityType = entity.getValue();

            metadataFactory.addEntity(entityFQN, entityType);
        }

        odataSchemas = metadataFactory.createSchema("");
    }

}
