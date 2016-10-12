/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.repository.store.graph.v1;

import com.google.common.base.Preconditions;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.typedef.AtlasBaseTypeDef;
import org.apache.atlas.model.typedef.AtlasClassificationDef;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.model.typedef.AtlasEnumDef;
import org.apache.atlas.model.typedef.AtlasStructDef;
import org.apache.atlas.repository.Constants;
import org.apache.atlas.repository.graph.AtlasGraphProvider;
import org.apache.atlas.repository.graphdb.AtlasEdge;
import org.apache.atlas.repository.graphdb.AtlasEdgeDirection;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.AtlasTypeDefGraphStore;
import org.apache.atlas.typesystem.types.DataTypes.TypeCategory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.apache.atlas.repository.Constants.TYPE_CATEGORY_PROPERTY_KEY;
import static org.apache.atlas.repository.Constants.VERTEX_TYPE_PROPERTY_KEY;
import static org.apache.atlas.repository.store.graph.v1.AtlasGraphUtilsV1.VERTEX_TYPE;


/**
 * Graph persistence store for TypeDef - v1
 */
public class AtlasTypeDefGraphStoreV1 extends AtlasTypeDefGraphStore {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasTypeDefGraphStoreV1.class);

    protected final AtlasGraph atlasGraph = AtlasGraphProvider.getGraphInstance();

    public AtlasTypeDefGraphStoreV1() {
        super();

        enumDefStore           = new AtlasEnumDefStoreV1(this);
        structDefStore         = new AtlasStructDefStoreV1(this);
        classificationDefStore = new AtlasClassificationDefStoreV1(this);
        entityDefStore         = new AtlasEntityDefStoreV1(this);
    }

    @Override
    public void init() {

    }

    public AtlasGraph getAtlasGraph() { return atlasGraph; }

    public AtlasVertex findTypeVertexByName(String typeName) {
        Iterator results = atlasGraph.query().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                                             .has(Constants.TYPENAME_PROPERTY_KEY, typeName)
                                             .vertices().iterator();

        AtlasVertex ret = (results != null && results.hasNext()) ? (AtlasVertex) results.next() : null;

        return ret;
    }

    public AtlasVertex findTypeVertexByNameAndCategory(String typeName, TypeCategory category) {
        Iterator results = atlasGraph.query().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                                             .has(Constants.TYPENAME_PROPERTY_KEY, typeName)
                                             .has(TYPE_CATEGORY_PROPERTY_KEY, category)
                                             .vertices().iterator();

        AtlasVertex ret = (results != null && results.hasNext()) ? (AtlasVertex) results.next() : null;

        return ret;
    }

    public AtlasVertex findTypeVertexByGuid(String typeGuid) {
        Iterator<AtlasVertex> vertices = atlasGraph.query().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                                                      .has(Constants.GUID_PROPERTY_KEY, typeGuid)
                                                      .vertices().iterator();

        AtlasVertex ret = (vertices != null && vertices.hasNext()) ? vertices.next() : null;

        return ret;
    }

    public AtlasVertex findTypeVertexByGuidAndCategory(String typeGuid, TypeCategory category) {
        Iterator<AtlasVertex> vertices = atlasGraph.query().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                                                      .has(Constants.GUID_PROPERTY_KEY, typeGuid)
                                                      .has(TYPE_CATEGORY_PROPERTY_KEY, category)
                                                      .vertices().iterator();

        AtlasVertex ret = (vertices != null && vertices.hasNext()) ? vertices.next() : null;

        return ret;
    }

    public Iterator<AtlasVertex> findTypeVerticesByCategory(TypeCategory category) {
        Iterator<AtlasVertex> ret = atlasGraph.query().has(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE)
                                                 .has(TYPE_CATEGORY_PROPERTY_KEY, category)
                                                 .vertices().iterator();

        return ret;
    }

    public AtlasVertex createTypeVertex(AtlasBaseTypeDef typeDef) {
        // Validate all the required checks
        Preconditions.checkArgument(StringUtils.isNotBlank(typeDef.getName()), "Type name can't be null/empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(typeDef.getTypeVersion()), "Type version can't be null/empty");
        Preconditions.checkArgument(typeDef.getVersion() != null, "Version can't be null");

        AtlasVertex ret = atlasGraph.addVertex();

        if (StringUtils.isBlank(typeDef.getTypeVersion())) {
            typeDef.setTypeVersion("1.0");
        }

        if (StringUtils.isBlank(typeDef.getGuid())) {
            typeDef.setGuid(UUID.randomUUID().toString());
        }

        if (typeDef.getCreateTime() == null) {
            typeDef.setCreateTime(new Date());
        }

        if (typeDef.getUpdateTime() == null) {
            typeDef.setUpdateTime(new Date());
        }

        ret.setProperty(VERTEX_TYPE_PROPERTY_KEY, VERTEX_TYPE); // Mark as type vertex
        ret.setProperty(TYPE_CATEGORY_PROPERTY_KEY, getTypeCategory(typeDef));

        ret.setProperty(Constants.TYPENAME_PROPERTY_KEY, typeDef.getName());
        ret.setProperty(Constants.TYPEDESCRIPTION_PROPERTY_KEY,
                StringUtils.isNotBlank(typeDef.getDescription()) ? typeDef.getDescription() : typeDef.getName());
        ret.setProperty(Constants.TYPEVERSION_PROPERTY_KEY, typeDef.getTypeVersion());
        ret.setProperty(Constants.GUID_PROPERTY_KEY, typeDef.getGuid());
        ret.setProperty(Constants.TIMESTAMP_PROPERTY_KEY, typeDef.getCreateTime().getTime());
        ret.setProperty(Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY, typeDef.getUpdateTime().getTime());
        ret.setProperty(Constants.VERSION_PROPERTY_KEY, typeDef.getVersion());

        return ret;
    }

    public void deleteTypeVertex(AtlasVertex vertex) throws AtlasBaseException {
        Iterator<AtlasEdge> inEdges = vertex.getEdges(AtlasEdgeDirection.IN).iterator();

        if (inEdges.hasNext()) {
            throw new AtlasBaseException("has references");
        }

        Iterable<AtlasEdge> edges = vertex.getEdges(AtlasEdgeDirection.OUT);

        for (AtlasEdge edge : edges) {
            atlasGraph.removeEdge(edge);
        }

        atlasGraph.removeVertex(vertex);
    }

    public void vertexToTypeDef(AtlasVertex vertex, AtlasBaseTypeDef typeDef) {
        String name        = vertex.getProperty(Constants.TYPENAME_PROPERTY_KEY, String.class);
        String description = vertex.getProperty(Constants.TYPEDESCRIPTION_PROPERTY_KEY, String.class);
        String typeVersion = vertex.getProperty(Constants.TYPEVERSION_PROPERTY_KEY, String.class);
        String guid        = vertex.getProperty(Constants.GUID_PROPERTY_KEY, String.class);
        Long   createTime  = vertex.getProperty(Constants.TIMESTAMP_PROPERTY_KEY, Long.class);
        Long   updateTime  = vertex.getProperty(Constants.MODIFICATION_TIMESTAMP_PROPERTY_KEY, Long.class);
        Long   version     = vertex.getProperty(Constants.VERSION_PROPERTY_KEY, Long.class);

        typeDef.setName(name);
        typeDef.setDescription(description);
        typeDef.setTypeVersion(typeVersion);
        typeDef.setGuid(guid);

        if (createTime != null) {
            typeDef.setCreateTime(new Date(createTime));
        }

        if (updateTime != null) {
            typeDef.setUpdateTime(new Date(updateTime));
        }

        if (version != null) {
            typeDef.setVersion(version);
        }
    }

    public boolean isTypeVertex(AtlasVertex vertex) {
        String vertexType = vertex.getProperty(Constants.VERTEX_TYPE_PROPERTY_KEY, String.class);

        boolean ret = VERTEX_TYPE.equals(vertexType);

        return ret;
    }

    public boolean isTypeVertex(AtlasVertex vertex, TypeCategory category) {
        boolean ret = false;

        if (isTypeVertex(vertex)) {
            TypeCategory vertexCategory = vertex.getProperty(Constants.TYPE_CATEGORY_PROPERTY_KEY, TypeCategory.class);

            ret = category.equals(vertexCategory);
        }

        return ret;
    }

    public boolean isTypeVertex(AtlasVertex vertex, TypeCategory[] categories) {
        boolean ret = false;

        if (isTypeVertex(vertex)) {
            TypeCategory vertexCategory = vertex.getProperty(TYPE_CATEGORY_PROPERTY_KEY, TypeCategory.class);

            for (TypeCategory category : categories) {
                if (category.equals(vertexCategory)) {
                    ret = true;

                    break;
                }
            }
        }

        return ret;
    }

    public AtlasEdge getOrCreateEdge(AtlasVertex outVertex, AtlasVertex inVertex, String edgeLabel) {
        AtlasEdge           ret   = null;
        Iterable<AtlasEdge> edges = outVertex.getEdges(AtlasEdgeDirection.OUT, edgeLabel);

        for (AtlasEdge edge : edges) {
            if (edge.getInVertex().getId().equals(inVertex.getId())) {
                ret = edge;
                break;
            }
        }

        if (ret == null) {
            ret = addEdge(outVertex, inVertex, edgeLabel);
        }

        return ret;
    }

    public AtlasEdge addEdge(AtlasVertex outVertex, AtlasVertex inVertex, String edgeLabel) {
        AtlasEdge ret = atlasGraph.addEdge(outVertex, inVertex, edgeLabel);

        return ret;
    }

    public void createSuperTypeEdges(AtlasVertex vertex, Set<String> superTypes) {
        if (CollectionUtils.isNotEmpty(superTypes)) {
            for (String superType : superTypes) {
                AtlasVertex superTypeVertex = findTypeVertexByNameAndCategory(superType, TypeCategory.CLASS);

                getOrCreateEdge(vertex, superTypeVertex, AtlasGraphUtilsV1.SUPERTYPE_EDGE_LABEL);
            }
        }
        // TODO: remove any other superType edges, if any exists
    }

    public Set<String> getSuperTypeNames(AtlasVertex vertex) {
        Set<String>    ret   = new HashSet<>();
        Iterable<AtlasEdge> edges = vertex.getEdges(AtlasEdgeDirection.OUT, AtlasGraphUtilsV1.SUPERTYPE_EDGE_LABEL);

        for (AtlasEdge edge : edges) {
            ret.add(edge.getInVertex().getProperty(Constants.TYPENAME_PROPERTY_KEY, String.class));
        }

        return ret;
    }

    private TypeCategory getTypeCategory(AtlasBaseTypeDef typeDef) {
        TypeCategory ret = null;

        if (typeDef instanceof AtlasEntityDef) {
            ret = TypeCategory.CLASS;
        } else if (typeDef instanceof AtlasClassificationDef) {
            ret = TypeCategory.TRAIT;
        } else if (typeDef instanceof AtlasStructDef) {
            ret = TypeCategory.STRUCT;
        } else if (typeDef instanceof AtlasEnumDef) {
            ret = TypeCategory.ENUM;
        }

        return ret;
    }
}