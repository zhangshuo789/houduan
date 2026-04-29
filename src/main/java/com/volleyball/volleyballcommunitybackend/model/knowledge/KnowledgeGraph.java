package com.volleyball.volleyballcommunitybackend.model.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraph {
    /** 节点列表 */
    private List<KnowledgeNode> nodes;
    /** 关系列表 */
    private List<KnowledgeEdge> edges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeEdge {
        /** 源节点 elementId */
        private String from;
        /** 目标节点 elementId */
        private String to;
        /** 关系类型 */
        private String label;
        /** 关系额外属性 */
        private java.util.Map<String, Object> properties;
    }
}
