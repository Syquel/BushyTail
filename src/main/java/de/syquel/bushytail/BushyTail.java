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
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private List<CsdlSchema> odataSchemas;
    private Map<Class<?>, IBushyTailController<?>> entityControllerMap;
    private Map<FullQualifiedName, Class<?>> entityTypeMap;

    BushyTail(List<CsdlSchema> odataSchemas, Map<Class<?>, IBushyTailController<?>> entityControllerMap, Map<FullQualifiedName, Class<?>> entityTypeMap) {
        this.odataSchemas = odataSchemas;
        this.entityControllerMap = entityControllerMap;
        this.entityTypeMap = entityTypeMap;
    }

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

}
