package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.model.knowledge.EntityType;
import com.volleyball.volleyballcommunitybackend.model.knowledge.KnowledgeGraph;
import com.volleyball.volleyballcommunitybackend.model.knowledge.KnowledgeNode;
import com.volleyball.volleyballcommunitybackend.model.knowledge.RelationType;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    private final Driver neo4jDriver;

    public KnowledgeService(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    // ==================== 实体 CRUD ====================

    /**
     * 创建知识实体
     */
    public KnowledgeNode createEntity(String name, EntityType type, String description,
                                       Map<String, Object> properties, Long createdBy) {
        try (var session = neo4jDriver.session()) {
            String id = UUID.randomUUID().toString();
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            params.put("name", name);
            params.put("type", type.name());
            params.put("description", description != null ? description : "");
            params.put("createdAt", LocalDateTime.now().toString());

            StringBuilder cypher = new StringBuilder();
            cypher.append("CREATE (n:").append(type.name()).append(" {");
            cypher.append("id: $id, name: $name, type: $type, description: $description, createdAt: $createdAt");
            cypher.append("}) ");

            if (properties != null && !properties.isEmpty()) {
                cypher.append("SET n += $properties ");
                params.put("properties", properties);
            }

            cypher.append("RETURN n, elementId(n) as elementId");

            var record = session.run(cypher.toString(), params).single();
            return mapToNode(record.get("n").asNode(), record.get("elementId").asString());
        }
    }

    /**
     * 更新知识实体
     */
    public KnowledgeNode updateEntity(String entityId, String name, String description,
                                       Map<String, Object> properties) {
        try (var session = neo4jDriver.session()) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", entityId);

            StringBuilder setClauses = new StringBuilder();
            if (name != null && !name.isBlank()) {
                params.put("name", name);
                setClauses.append("n.name = $name, ");
            }
            if (description != null && !description.isBlank()) {
                params.put("description", description);
                setClauses.append("n.description = $description, ");
            }
            if (properties != null && !properties.isEmpty()) {
                params.put("properties", properties);
                setClauses.append("n += $properties, ");
            }

            if (setClauses.isEmpty()) {
                return getEntityById(entityId);
            }

            String setStr = setClauses.substring(0, setClauses.length() - 2);
            String cypher = "MATCH (n {id: $id}) SET " + setStr + " RETURN n, elementId(n) as elementId";

            var record = session.run(cypher, params).single();
            return mapToNode(record.get("n").asNode(), record.get("elementId").asString());
        }
    }

    /**
     * 删除实体（同时删除其所有关系）
     */
    public void deleteEntity(String entityId) {
        try (var session = neo4jDriver.session()) {
            session.run("MATCH (n {id: $id}) DETACH DELETE n", Map.of("id", entityId));
        }
    }

    /**
     * 根据业务 ID 获取实体
     */
    public KnowledgeNode getEntityById(String entityId) {
        try (var session = neo4jDriver.session()) {
            var result = session.run(
                "MATCH (n {id: $id}) RETURN n, elementId(n) as elementId",
                Map.of("id", entityId)
            );
            if (!result.hasNext()) {
                return null;
            }
            var record = result.single();
            return mapToNode(record.get("n").asNode(), record.get("elementId").asString());
        }
    }

    // ==================== 关系 CRUD ====================

    /**
     * 创建两实体之间的关系
     */
    public void createRelation(String fromId, String toId, RelationType type,
                               Map<String, Object> properties) {
        try (var session = neo4jDriver.session()) {
            Map<String, Object> params = new HashMap<>();
            params.put("fromId", fromId);
            params.put("toId", toId);

            String cypher = "MATCH (a {id: $fromId}), (b {id: $toId}) " +
                           "MERGE (a)-[r:" + type.name() + "]->(b) ";

            if (properties != null && !properties.isEmpty()) {
                params.put("props", properties);
                cypher += "SET r += $props ";
            }

            cypher += "RETURN r";
            session.run(cypher, params).consume();
        }
    }

    /**
     * 删除关系
     */
    public void deleteRelation(String fromId, String toId, String relationType) {
        try (var session = neo4jDriver.session()) {
            session.run(
                "MATCH (a {id: $fromId})-[r:" + relationType + "]->(b {id: $toId}) DELETE r",
                Map.of("fromId", fromId, "toId", toId)
            );
        }
    }

    // ==================== 图谱查询 ====================

    /**
     * 根据实体 ID（UUID）获取图谱，自动展开关联节点（2 层）
     */
    public KnowledgeGraph getEntityGraph(String entityId) {
        Set<KnowledgeNode> nodeSet = new LinkedHashSet<>();
        Set<KnowledgeGraph.KnowledgeEdge> edgeSet = new LinkedHashSet<>();

        try (var session = neo4jDriver.session()) {
            // 1. 查找目标实体
            var entityResult = session.run(
                "MATCH (n {id: $id}) RETURN n, elementId(n) as eid, labels(n) as labels",
                Map.of("id", entityId)
            );
            if (!entityResult.hasNext()) {
                log.warn("getEntityGraph: 未找到实体 id={}", entityId);
                return KnowledgeGraph.builder()
                    .nodes(Collections.emptyList())
                    .edges(Collections.emptyList())
                    .build();
            }
            var entityRecord = entityResult.single();
            var entityNode = mapToNode(entityRecord.get("n").asNode(), entityRecord.get("eid").asString());
            nodeSet.add(entityNode);

            // 2. 查询所有直接关联节点（1 层展开）
            var directResult = session.run("""
                MATCH (n {id: $id})-[r]-(neighbor)
                RETURN neighbor, r, type(r) as rtype,
                       elementId(neighbor) as neighborEid,
                       startNode(r) as sn, endNode(r) as en
                """, Map.of("id", entityId));
            while (directResult.hasNext()) {
                var rec = directResult.next();
                var neighborNode = mapToNode(rec.get("neighbor").asNode(), rec.get("neighborEid").asString());
                if (nodeSet.add(neighborNode)) {
                    edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                        .from(rec.get("sn").asNode().elementId())
                        .to(rec.get("en").asNode().elementId())
                        .label(rec.get("rtype").asString())
                        .build());
                }
            }

            // 3. 查询第二层关联节点（通过展开后的节点进一步关联）
            for (var node : new ArrayList<>(nodeSet)) {
                var indirectResult = session.run("""
                    MATCH (n {id: $nodeId})-[r]-(neighbor)
                    RETURN neighbor, r, type(r) as rtype,
                           elementId(neighbor) as neighborEid,
                           startNode(r) as sn, endNode(r) as en
                    """, Map.of("nodeId", node.getId()));
                while (indirectResult.hasNext()) {
                    var rec = indirectResult.next();
                    var neighborNode = mapToNode(rec.get("neighbor").asNode(), rec.get("neighborEid").asString());
                    if (nodeSet.add(neighborNode)) {
                        edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                            .from(rec.get("sn").asNode().elementId())
                            .to(rec.get("en").asNode().elementId())
                            .label(rec.get("rtype").asString())
                            .build());
                    }
                }
            }
        }

        return KnowledgeGraph.builder()
            .nodes(new ArrayList<>(nodeSet))
            .edges(new ArrayList<>(edgeSet))
            .build();
    }

    /**
     * 获取球员知识图谱
     */
    public KnowledgeGraph getPlayerGraph(String name) {
        // 先搜后查
        var entities = searchEntities(name, "PLAYER");
        if (entities.isEmpty()) {
            return KnowledgeGraph.builder()
                .nodes(Collections.emptyList())
                .edges(Collections.emptyList())
                .build();
        }
        // 精确匹配
        for (var e : entities) {
            if (name.equals(e.getName())) {
                return getEntityGraph(e.getId());
            }
        }
        // 没精确匹配就用第一个
        return getEntityGraph(entities.get(0).getId());
    }

    /**
     * 获取球队知识图谱
     */
    public KnowledgeGraph getTeamGraph(String name) {
        // 先搜后查
        var entities = searchEntities(name, "TEAM");
        if (entities.isEmpty()) {
            return KnowledgeGraph.builder()
                .nodes(Collections.emptyList())
                .edges(Collections.emptyList())
                .build();
        }
        for (var e : entities) {
            if (name.equals(e.getName())) {
                return getEntityGraph(e.getId());
            }
        }
        return getEntityGraph(entities.get(0).getId());
    }

    /**
     * 搜索实体（按名称模糊匹配）
     */
    public List<KnowledgeNode> searchEntities(String keyword, String type) {
        List<KnowledgeNode> nodes = new ArrayList<>();
        try (var session = neo4jDriver.session()) {
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", keyword.toLowerCase());

            StringBuilder cypher = new StringBuilder("MATCH (n) WHERE toLower(n.name) CONTAINS $keyword");
            if (type != null && !type.isBlank()) {
                cypher.append(" AND n.type = $type");
                params.put("type", type);
            }
            cypher.append(" RETURN n, elementId(n) as eid ORDER BY n.name LIMIT 50");

            var result = session.run(cypher.toString(), params);
            while (result.hasNext()) {
                var record = result.next();
                nodes.add(mapToNode(record.get("n").asNode(), record.get("eid").asString()));
            }
        }
        return nodes;
    }

    /**
     * 查询两实体之间的最短路径
     */
    public KnowledgeGraph findShortestPath(String fromName, String toName) {
        try (var session = neo4jDriver.session()) {
            var result = session.run("""
                MATCH path = shortestPath((a {name: $from})-[*1..5]-(b {name: $to}))
                RETURN nodes(path) as pathNodes, relationships(path) as pathRels
                """, Map.of("from", fromName, "to", toName));

            if (!result.hasNext()) {
                return KnowledgeGraph.builder()
                    .nodes(Collections.emptyList())
                    .edges(Collections.emptyList())
                    .build();
            }

            var record = result.single();
            List<KnowledgeNode> nodes = new ArrayList<>();
            List<KnowledgeGraph.KnowledgeEdge> edges = new ArrayList<>();

            for (var node : record.get("pathNodes").asList(Value::asNode)) {
                nodes.add(mapToNode(node, node.elementId()));
            }
            for (var rel : record.get("pathRels").asList(Value::asRelationship)) {
                edges.add(KnowledgeGraph.KnowledgeEdge.builder()
                    .from(rel.startNodeElementId())
                    .to(rel.endNodeElementId())
                    .label(rel.type())
                    .build());
            }

            return KnowledgeGraph.builder().nodes(nodes).edges(edges).build();
        }
    }

    /**
     * 按类型浏览实体
     */
    public List<KnowledgeNode> getEntitiesByType(EntityType type) {
        List<KnowledgeNode> nodes = new ArrayList<>();
        try (var session = neo4jDriver.session()) {
            var result = session.run(
                "MATCH (n:" + type.name() + ") RETURN n, elementId(n) as eid ORDER BY n.name LIMIT 100",
                Map.of()
            );
            while (result.hasNext()) {
                var record = result.next();
                nodes.add(mapToNode(record.get("n").asNode(), record.get("eid").asString()));
            }
        }
        return nodes;
    }

    // ==================== 诊断 ====================

    /**
     * 获取 Neo4j 连接健康信息和实体统计
     */
    public Map<String, Object> getHealthInfo() {
        Map<String, Object> info = new HashMap<>();
        try (var session = neo4jDriver.session()) {
            session.run("RETURN 1").consume();
            info.put("connected", true);
            info.put("uri", "已连接"); // Driver 内部持有
        } catch (Exception e) {
            info.put("connected", false);
            info.put("error", e.getMessage());
        }
        info.put("entityCount", countEntities());
        info.put("relationCount", countRelations());
        return info;
    }

    public long countEntities() {
        try (var session = neo4jDriver.session()) {
            var result = session.run("MATCH (n) RETURN count(n) AS cnt");
            return result.single().get("cnt").asLong();
        } catch (Exception e) {
            return -1;
        }
    }

    public long countRelations() {
        try (var session = neo4jDriver.session()) {
            var result = session.run("MATCH ()-[r]->() RETURN count(r) AS cnt");
            return result.single().get("cnt").asLong();
        } catch (Exception e) {
            return -1;
        }
    }

    // ==================== 工具方法 ====================

    private KnowledgeNode mapToNode(Node neo4jNode, String elementId) {
        var props = new HashMap<String, Object>();
        // 复制所有属性
        for (var key : neo4jNode.keys()) {
            if (!key.equals("id") && !key.equals("name") && !key.equals("type")
                && !key.equals("description") && !key.equals("createdAt")) {
                var value = neo4jNode.get(key);
                if (!value.isNull()) {
                    props.put(key, value.asObject());
                }
            }
        }

        return KnowledgeNode.builder()
            .elementId(elementId)
            .id(neo4jNode.get("id").asString(null))
            .name(neo4jNode.get("name").asString(null))
            .type(neo4jNode.get("type").asString(null))
            .description(neo4jNode.get("description").asString(null))
            .properties(props)
            .build();
    }
}
