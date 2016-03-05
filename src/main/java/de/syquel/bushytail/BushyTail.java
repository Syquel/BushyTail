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

import de.syquel.bushytail.service.BushyTailEdmProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The BushyTail class should be used by the developer.
 * <p/>
 * It converts JPA entities and {@link de.syquel.bushytail.controller.IBushyTailController}s to an Olingo OData Version 4 service.
 *
 * @author Clemens Bartz
 * @since 1.0
 */
public class BushyTail {

    /**
     * Serve a request from a Servlet.
     *
     * @param req the request
     * @param resp the response which will be modified
     * @throws ServletException
     * @throws IOException
     */
    public void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            final OData oData = OData.newInstance();
            final ServiceMetadata edm = oData.createServiceMetadata(new BushyTailEdmProvider(), new ArrayList<EdmxReference>(0));

            final ODataHttpHandler handler = oData.createHandler(edm);
            handler.process(req, resp);
        } catch (final RuntimeException e) {
            throw new ServletException(e);
        }
    }
}
