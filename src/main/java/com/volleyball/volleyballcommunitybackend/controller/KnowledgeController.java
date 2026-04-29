package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.CreateEntityRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.CreateRelationRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.model.knowledge.EntityType;
import com.volleyball.volleyballcommunitybackend.model.knowledge.KnowledgeGraph;
import com.volleyball.volleyballcommunitybackend.model.knowledge.KnowledgeNode;
import com.volleyball.volleyballcommunitybackend.model.knowledge.RelationType;
import com.volleyball.volleyballcommunitybackend.service.KnowledgeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    // ==================== 实体管理 ====================

    /**
     * 搜索实体（按名称模糊匹配，可选按类型过滤）
     */
    @GetMapping("/entity/search")
    public ResponseEntity<ApiResponse<List<KnowledgeNode>>> searchEntities(
            @RequestParam String keyword,
            @RequestParam(required = false) String type) {
        List<KnowledgeNode> nodes = knowledgeService.searchEntities(keyword, type);
        return ResponseEntity.ok(ApiResponse.success(nodes));
    }

    /**
     * 获取实体详情
     */
    @GetMapping("/entity/{id}")
    public ResponseEntity<ApiResponse<KnowledgeNode>> getEntity(@PathVariable String id) {
        KnowledgeNode node = knowledgeService.getEntityById(id);
        if (node == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "实体不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(node));
    }

    /**
     * 创建实体
     */
    @PostMapping("/entity")
    public ResponseEntity<ApiResponse<KnowledgeNode>> createEntity(
            @Valid @RequestBody CreateEntityRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        EntityType type = EntityType.valueOf(request.getType().toUpperCase());
        KnowledgeNode node = knowledgeService.createEntity(
                request.getName(), type, request.getDescription(), request.getProperties(), userId);
        return ResponseEntity.ok(ApiResponse.success("实体创建成功", node));
    }

    /**
     * 更新实体
     */
    @PutMapping("/entity/{id}")
    public ResponseEntity<ApiResponse<KnowledgeNode>> updateEntity(
            @PathVariable String id,
            @Valid @RequestBody CreateEntityRequest request) {
        KnowledgeNode node = knowledgeService.updateEntity(id, request.getName(),
                request.getDescription(), request.getProperties());
        if (node == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "实体不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("实体更新成功", node));
    }

    /**
     * 删除实体
     */
    @DeleteMapping("/entity/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntity(@PathVariable String id) {
        knowledgeService.deleteEntity(id);
        return ResponseEntity.ok(ApiResponse.success("实体已删除", null));
    }

    // ==================== 关系管理 ====================

    /**
     * 创建关系
     */
    @PostMapping("/relation")
    public ResponseEntity<ApiResponse<Void>> createRelation(
            @Valid @RequestBody CreateRelationRequest request) {
        RelationType type = RelationType.valueOf(request.getRelationType().toUpperCase());
        knowledgeService.createRelation(request.getFromId(), request.getToId(), type, request.getProperties());
        return ResponseEntity.ok(ApiResponse.success("关系创建成功", null));
    }

    /**
     * 删除关系
     */
    @DeleteMapping("/relation/{fromId}/{toId}/{type}")
    public ResponseEntity<ApiResponse<Void>> deleteRelation(
            @PathVariable String fromId,
            @PathVariable String toId,
            @PathVariable String type) {
        knowledgeService.deleteRelation(fromId, toId, type);
        return ResponseEntity.ok(ApiResponse.success("关系已删除", null));
    }

    // ==================== 图谱查询 ====================

    /**
     * 根据实体 ID（UUID）获取图谱
     * 示例: GET /api/knowledge/graph/entity?id=550e8400-...
     */
    @GetMapping("/graph/entity")
    public ResponseEntity<ApiResponse<KnowledgeGraph>> getEntityGraph(@RequestParam String id) {
        KnowledgeGraph graph = knowledgeService.getEntityGraph(id);
        return ResponseEntity.ok(ApiResponse.success(graph));
    }

    /**
     * 获取球员知识图谱
     * 示例: GET /api/knowledge/graph/player?name=朱婷
     */
    @GetMapping("/graph/player")
    public ResponseEntity<ApiResponse<KnowledgeGraph>> getPlayerGraph(@RequestParam String name) {
        KnowledgeGraph graph = knowledgeService.getPlayerGraph(name);
        return ResponseEntity.ok(ApiResponse.success(graph));
    }

    /**
     * 获取球队知识图谱
     * 示例: GET /api/knowledge/graph/team?name=中国女排
     */
    @GetMapping("/graph/team")
    public ResponseEntity<ApiResponse<KnowledgeGraph>> getTeamGraph(@RequestParam String name) {
        KnowledgeGraph graph = knowledgeService.getTeamGraph(name);
        return ResponseEntity.ok(ApiResponse.success(graph));
    }

    /**
     * 查询两实体之间的最短路径
     * 示例: GET /api/knowledge/path?from=朱婷&to=巴西女排
     */
    @GetMapping("/path")
    public ResponseEntity<ApiResponse<KnowledgeGraph>> findShortestPath(
            @RequestParam String from,
            @RequestParam String to) {
        KnowledgeGraph path = knowledgeService.findShortestPath(from, to);
        return ResponseEntity.ok(ApiResponse.success(path));
    }

    // ==================== 知识浏览 ====================

    /**
     * 按类型浏览实体
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<KnowledgeNode>>> getEntitiesByType(
            @PathVariable String type) {
        EntityType entityType = EntityType.valueOf(type.toUpperCase());
        List<KnowledgeNode> nodes = knowledgeService.getEntitiesByType(entityType);
        return ResponseEntity.ok(ApiResponse.success(nodes));
    }

    /**
     * 获取所有实体类型
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getEntityTypes() {
        List<Map<String, String>> types = new java.util.ArrayList<>();
        for (EntityType t : EntityType.values()) {
            types.add(Map.of("name", t.name(), "label", t.getLabel()));
        }
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    /**
     * 获取所有关系类型
     */
    @GetMapping("/relation-types")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getRelationTypes() {
        List<Map<String, String>> types = new java.util.ArrayList<>();
        for (RelationType t : RelationType.values()) {
            types.add(Map.of("name", t.name(), "label", t.getLabel()));
        }
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    /**
     * Neo4j 连接诊断
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> info = knowledgeService.getHealthInfo();
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
