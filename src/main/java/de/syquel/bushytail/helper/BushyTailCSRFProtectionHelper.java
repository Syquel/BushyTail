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

package de.syquel.bushytail.helper;

import org.apache.commons.codec.binary.Base64;
import org.apache.olingo.commons.api.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;

/**
 * Helper class for protection against CSRF attacks.
 *
 * @author Frederik Boster
 */
public class BushyTailCSRFProtectionHelper {

    /** The HTTP header attribute. */
    private static final String CSRF_TOKEN_HEADER_ATTRIBUTE = "X-CSRF-TOKEN";

    /** CSRF attribute name. */
    private static final String BUSHYTAIL_CSRFTOKEN_ATTRIBUTENAME = "BushyTailCsrfToken";

    /** Secure RNG generator. */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Initialize a new BushyTailCSRFProtectionHelper with a new {@link SecureRandom} Random Number Generator.
     */
    public BushyTailCSRFProtectionHelper() {
    }

    /**
     * Check the HTTP request and either insert a new CSRF token to the HTTP response for HTTP GET requests
     * or verify the existing one for every other HTTP method.
     *
     * @param request The HTTP request to extract the X-CSRF-TOKEN header from.
     * @param response The HTTP response to add a generated X-CSRF-TOKEN header to.
     * @throws BushyTailCSRFProtectionException If the verification of the CSRF token failed.
     */
    public void process(HttpServletRequest request, HttpServletResponse response) throws BushyTailCSRFProtectionException {
        final String httpMethod = request.getMethod();

        // Create CSRF token if HTTP method is GET; otherwise verify it.
        if (HttpMethod.GET.name().equalsIgnoreCase(httpMethod)) {
            createToken(request, response);
        } else {
            verifyToken(request);
        }
    }

    /**
     * Generate a new CSRF token and add it to the HTTP response.
     *
     * @param request The HTTP request to extract the X-CSRF-TOKEN header from.
     * @param response The HTTP response to add a generated X-CSRF-TOKEN header to.
     */
    private void createToken(HttpServletRequest request, HttpServletResponse response) {
        // Only generate new CSRF token if requested by client
        String csrfHeader = request.getHeader(CSRF_TOKEN_HEADER_ATTRIBUTE);
        if (!"FETCH".equalsIgnoreCase(csrfHeader)) {
            return;
        }

        // Get random bytes and encode in BASE64 format for CSRF token
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String csrfToken = Base64.encodeBase64String(randomBytes);

        // Save new CSRF token in HTTP session for verification
        HttpSession session = request.getSession();
        session.setAttribute(BUSHYTAIL_CSRFTOKEN_ATTRIBUTENAME, csrfToken);

        // Add new CSRF token to HTTP response
        response.addHeader(CSRF_TOKEN_HEADER_ATTRIBUTE, csrfToken);
    }

    /**
     * Verify the CSRF token.
     *
     * @param request The HTTP request to extract the X-CSRF-TOKEN header from.
     * @throws BushyTailCSRFProtectionException If the verification of the CSRF token failed.
     */
    private void verifyToken(HttpServletRequest request) throws BushyTailCSRFProtectionException {
        String csrfToken = request.getHeader(CSRF_TOKEN_HEADER_ATTRIBUTE);

        // Retrieve the valid CSRF token from the HTTP session
        HttpSession session = request.getSession();
        String cachedCsrfToken = (String) session.getAttribute(BUSHYTAIL_CSRFTOKEN_ATTRIBUTENAME);

        // Check if the valid CSRF token matches the one of the client
        Boolean isValidToken = (cachedCsrfToken != null && cachedCsrfToken.equals(csrfToken));
        if (!isValidToken) {
            throw new BushyTailCSRFProtectionException(CSRF_TOKEN_HEADER_ATTRIBUTE);
        }
    }


    /**
     * Exception class for verification errors of the CSRF token.
     */
    public class BushyTailCSRFProtectionException extends RuntimeException {

        /**
         * Constructs a new runtime exception with <code>null</code> as its
         * detail message.  The cause is not initialized, and may subsequently be
         * initialized by a call to {@link #initCause}.
         */
        public BushyTailCSRFProtectionException() {
        }

        /**
         * Constructs a new runtime exception with the specified detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public BushyTailCSRFProtectionException(String message) {
            super(message);
        }

        /**
         * Constructs a new runtime exception with the specified detail message and
         * cause.  <p>Note that the detail message associated with
         * <code>cause</code> is <i>not</i> automatically incorporated in
         * this runtime exception's detail message.
         *
         * @param message the detail message (which is saved for later retrieval
         *                by the {@link #getMessage()} method).
         * @param cause   the cause (which is saved for later retrieval by the
         *                {@link #getCause()} method).  (A <tt>null</tt> value is
         *                permitted, and indicates that the cause is nonexistent or
         *                unknown.)
         * @since 1.4
         */
        public BushyTailCSRFProtectionException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new runtime exception with the specified cause and a
         * detail message of <tt>(cause==null ? null : cause.toString())</tt>
         * (which typically contains the class and detail message of
         * <tt>cause</tt>).  This constructor is useful for runtime exceptions
         * that are little more than wrappers for other throwables.
         *
         * @param cause the cause (which is saved for later retrieval by the
         *              {@link #getCause()} method).  (A <tt>null</tt> value is
         *              permitted, and indicates that the cause is nonexistent or
         *              unknown.)
         * @since 1.4
         */
        public BushyTailCSRFProtectionException(Throwable cause) {
            super(cause);
        }

    }

}
