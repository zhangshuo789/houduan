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

import java.time.LocalDateTime;
import java.util.*;

@Service
public class KnowledgeService {

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
     * 获取球员知识图谱（球员 → 球队 → 比赛 → 赛事，最多 3 层）
     */
    public KnowledgeGraph getPlayerGraph(String name) {
        Set<KnowledgeNode> nodeSet = new LinkedHashSet<>();
        Set<KnowledgeGraph.KnowledgeEdge> edgeSet = new LinkedHashSet<>();

        try (var session = neo4jDriver.session()) {
            // 1. 查找球员
            var playerResult = session.run(
                "MATCH (p:Player {name: $name}) RETURN p, elementId(p) as eid",
                Map.of("name", name)
            );
            if (!playerResult.hasNext()) {
                return KnowledgeGraph.builder()
                    .nodes(Collections.emptyList())
                    .edges(Collections.emptyList())
                    .build();
            }
            var playerRecord = playerResult.single();
            var playerNode = mapToNode(playerRecord.get("p").asNode(), playerRecord.get("eid").asString());
            nodeSet.add(playerNode);

            // 2. 查询关联的球队及关系
            var teamResult = session.run("""
                MATCH (p:Player {name: $name})-[r:PLAYS_FOR]->(t:Team)
                RETURN t, r, elementId(t) as tid,
                       elementId(p) as pid, type(r) as rtype
                """, Map.of("name", name));
            while (teamResult.hasNext()) {
                var rec = teamResult.next();
                var teamNode = mapToNode(rec.get("t").asNode(), rec.get("tid").asString());
                if (nodeSet.add(teamNode)) {
                    edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                        .from(rec.get("pid").asString())
                        .to(rec.get("tid").asString())
                        .label(rec.get("rtype").asString())
                        .build());
                }
            }

            // 3. 查询球队参加的比赛
            for (var node : new ArrayList<>(nodeSet)) {
                if ("TEAM".equals(node.getType())) {
                    var matchResult = session.run("""
                        MATCH (t:Team {id: $teamId})-[r:PARTICIPATES_IN]->(m:Match)
                        RETURN m, r, elementId(m) as mid,
                               elementId(t) as tid, type(r) as rtype
                        """, Map.of("teamId", node.getId()));
                    while (matchResult.hasNext()) {
                        var rec = matchResult.next();
                        var matchNode = mapToNode(rec.get("m").asNode(), rec.get("mid").asString());
                        if (nodeSet.add(matchNode)) {
                            edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                                .from(rec.get("tid").asString())
                                .to(rec.get("mid").asString())
                                .label(rec.get("rtype").asString())
                                .build());
                        }
                    }
                }
            }

            // 4. 查询比赛所属赛事
            for (var node : new ArrayList<>(nodeSet)) {
                if ("MATCH".equals(node.getType())) {
                    var tournamentResult = session.run("""
                        MATCH (m:Match {id: $matchId})-[r:BELONGS_TO]->(t:Tournament)
                        RETURN t, r, elementId(t) as tid,
                               elementId(m) as mid, type(r) as rtype
                        """, Map.of("matchId", node.getId()));
                    while (tournamentResult.hasNext()) {
                        var rec = tournamentResult.next();
                        var tournamentNode = mapToNode(rec.get("t").asNode(), rec.get("tid").asString());
                        if (nodeSet.add(tournamentNode)) {
                            edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                                .from(rec.get("mid").asString())
                                .to(rec.get("tid").asString())
                                .label(rec.get("rtype").asString())
                                .build());
                        }
                    }
                }
            }

            // 5. 查询同队队友
            var teammateResult = session.run("""
                MATCH (p:Player {name: $name})-[r:TEAMMATE_OF]-(mate:Player)
                RETURN mate, r, elementId(mate) as eid,
                       elementId(p) as pid, elementId(r) as rid,
                       startNode(r) as sn, endNode(r) as en, type(r) as rtype
                """, Map.of("name", name));
            while (teammateResult.hasNext()) {
                var rec = teammateResult.next();
                var mateNode = mapToNode(rec.get("mate").asNode(), rec.get("eid").asString());
                if (nodeSet.add(mateNode)) {
                    edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                        .from(rec.get("sn").asNode().elementId())
                        .to(rec.get("en").asNode().elementId())
                        .label(rec.get("rtype").asString())
                        .build());
                }
            }

            // 6. 查询教练关系
            var coachResult = session.run("""
                MATCH (p:Player {name: $name})-[r:COACHES]->(t:Team)
                RETURN t, r, elementId(t) as tid,
                       elementId(p) as pid, type(r) as rtype
                """, Map.of("name", name));
            while (coachResult.hasNext()) {
                var rec = coachResult.next();
                var teamNode = mapToNode(rec.get("t").asNode(), rec.get("tid").asString());
                if (nodeSet.add(teamNode)) {
                    edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                        .from(rec.get("pid").asString())
                        .to(rec.get("tid").asString())
                        .label(rec.get("rtype").asString())
                        .build());
                }
            }
        }

        return KnowledgeGraph.builder()
            .nodes(new ArrayList<>(nodeSet))
            .edges(new ArrayList<>(edgeSet))
            .build();
    }

    /**
     * 获取球队知识图谱
     */
    public KnowledgeGraph getTeamGraph(String name) {
        Set<KnowledgeNode> nodeSet = new LinkedHashSet<>();
        Set<KnowledgeGraph.KnowledgeEdge> edgeSet = new LinkedHashSet<>();

        try (var session = neo4jDriver.session()) {
            // 1. 球队
            var teamResult = session.run(
                "MATCH (t:Team {name: $name}) RETURN t, elementId(t) as eid",
                Map.of("name", name)
            );
            if (!teamResult.hasNext()) {
                return KnowledgeGraph.builder()
                    .nodes(Collections.emptyList()).edges(Collections.emptyList()).build();
            }
            var rec = teamResult.single();
            nodeSet.add(mapToNode(rec.get("t").asNode(), rec.get("eid").asString()));

            // 2. 球员
            var playerResult = session.run("""
                MATCH (p:Player)-[r:PLAYS_FOR]->(t:Team {name: $name})
                RETURN p, r, elementId(p) as pid,
                       elementId(t) as tid, type(r) as rtype
                """, Map.of("name", name));
            while (playerResult.hasNext()) {
                var pr = playerResult.next();
                if (nodeSet.add(mapToNode(pr.get("p").asNode(), pr.get("pid").asString()))) {
                    edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                        .from(pr.get("pid").asString())
                        .to(pr.get("tid").asString())
                        .label(pr.get("rtype").asString())
                        .build());
                }
            }

            // 3. 教练
            var coachResult = session.run("""
                MATCH (p:Player)-[r:COACHES]->(t:Team {name: $name})
                RETURN p, r, elementId(p) as pid,
                       elementId(t) as tid, type(r) as rtype
                """, Map.of("name", name));
            while (coachResult.hasNext()) {
                var cr = coachResult.next();
                if (nodeSet.add(mapToNode(cr.get("p").asNode(), cr.get("pid").asString()))) {
                    edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                        .from(cr.get("pid").asString())
                        .to(cr.get("tid").asString())
                        .label(cr.get("rtype").asString())
                        .build());
                }
            }

            // 4. 比赛
            for (var node : new ArrayList<>(nodeSet)) {
                if ("TEAM".equals(node.getType())) {
                    var matchResult = session.run("""
                        MATCH (t:Team {id: $teamId})-[r:PARTICIPATES_IN]->(m:Match)
                        RETURN m, r, elementId(m) as mid,
                               elementId(t) as tid, type(r) as rtype
                        """, Map.of("teamId", node.getId()));
                    while (matchResult.hasNext()) {
                        var mr = matchResult.next();
                        if (nodeSet.add(mapToNode(mr.get("m").asNode(), mr.get("mid").asString()))) {
                            edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                                .from(mr.get("tid").asString())
                                .to(mr.get("mid").asString())
                                .label(mr.get("rtype").asString())
                                .build());
                        }
                    }
                }
            }

            // 5. 赛事
            for (var node : new ArrayList<>(nodeSet)) {
                if ("MATCH".equals(node.getType())) {
                    var tournamentResult = session.run("""
                        MATCH (m:Match {id: $matchId})-[r:BELONGS_TO]->(t:Tournament)
                        RETURN t, r, elementId(t) as tid,
                               elementId(m) as mid, type(r) as rtype
                        """, Map.of("matchId", node.getId()));
                    while (tournamentResult.hasNext()) {
                        var tr = tournamentResult.next();
                        if (nodeSet.add(mapToNode(tr.get("t").asNode(), tr.get("tid").asString()))) {
                            edgeSet.add(KnowledgeGraph.KnowledgeEdge.builder()
                                .from(tr.get("mid").asString())
                                .to(tr.get("tid").asString())
                                .label(tr.get("rtype").asString())
                                .build());
                        }
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
