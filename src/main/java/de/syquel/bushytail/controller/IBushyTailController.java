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

package de.syquel.bushytail.controller;

import org.apache.olingo.server.api.uri.UriParameter;

import java.util.List;

/**
 * Interface for all ODataControllers. Provides CRUD Operations.
 *
 * @author Clemens Bartz
 * @author Frederik Boster
 * @since 1.0
 *
 * @param <T> Entity which is handled by the controller.
 */
public interface IBushyTailController<T> {

    /**
     * Read an entity.
     * @param keyPredicates the search criterias
     * @return the entity
     */
    T read(List<UriParameter> keyPredicates);

    /**
     * Create the entity.
     * @param entity the entity
     */
    void create(T entity);

    void update(T entity);

    void delete(T entity);

}
