/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.repository.ogm;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.model.profile.AtlasUserSavedSearch;
import org.apache.atlas.type.AtlasType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class AtlasSavedSearchDTO extends AbstractDataTransferObject<AtlasUserSavedSearch> {
    private static final String PROPERTY_NAME              = "name";
    private static final String PROPERTY_OWNER_NAME        = "ownerName";
    private static final String PROPERTY_SEARCH_PARAMETERS = "searchParameters";
    private static final String PROPERTY_UNIQUE_NAME       = "uniqueName";
    private static final String PROPERTY_SEARCH_TYPE       = "searchType";

    public AtlasSavedSearchDTO(AtlasTypeRegistry typeRegistry) {
        super(typeRegistry, AtlasUserSavedSearch.class);
    }

    @Override
    public AtlasUserSavedSearch from(AtlasEntity entity) {
        AtlasUserSavedSearch savedSearch = new AtlasUserSavedSearch();

        savedSearch.setGuid(entity.getGuid());
        savedSearch.setName((String) entity.getAttribute(PROPERTY_NAME));
        savedSearch.setOwnerName((String) entity.getAttribute(PROPERTY_OWNER_NAME));
        savedSearch.setSearchType(AtlasUserSavedSearch.SavedSearchType.to((String) entity.getAttribute(PROPERTY_SEARCH_TYPE)));

        String jsonSearchParams = (String) entity.getAttribute(PROPERTY_SEARCH_PARAMETERS);

        if (StringUtils.isNotEmpty(jsonSearchParams)) {
            savedSearch.setSearchParameters(AtlasType.fromJson(jsonSearchParams, SearchParameters.class));
        }

        return savedSearch;
    }

    @Override
    public AtlasUserSavedSearch from(AtlasEntityWithExtInfo entityWithExtInfo) {
        return from(entityWithExtInfo.getEntity());
    }

    @Override
    public AtlasEntity toEntity(AtlasUserSavedSearch obj) throws AtlasBaseException {
        AtlasEntity entity = getDefaultAtlasEntity(obj);

        entity.setAttribute(PROPERTY_NAME, obj.getName());
        entity.setAttribute(PROPERTY_OWNER_NAME, obj.getOwnerName());
        entity.setAttribute(PROPERTY_SEARCH_TYPE, obj.getSearchType());
        entity.setAttribute(PROPERTY_UNIQUE_NAME, getUniqueValue(obj));

        if (obj.getSearchParameters() != null) {
            entity.setAttribute(PROPERTY_SEARCH_PARAMETERS, AtlasType.toJson(obj.getSearchParameters()));
        }

        return entity;
    }

    @Override
    public AtlasEntityWithExtInfo toEntityWithExtInfo(AtlasUserSavedSearch obj) throws AtlasBaseException {
        return new AtlasEntityWithExtInfo(toEntity(obj));
    }

    @Override
    public Map<String, Object> getUniqueAttributes(AtlasUserSavedSearch obj) {
        Map<String, Object> ret = new HashMap<>();

        ret.put(PROPERTY_UNIQUE_NAME, getUniqueValue(obj));

        return ret;
    }

    private String getUniqueValue(AtlasUserSavedSearch obj) {
        return String.format("%s:%s", obj.getOwnerName(), obj.getName()) ;
    }
}
