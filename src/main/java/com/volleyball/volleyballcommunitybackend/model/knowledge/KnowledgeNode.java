package com.volleyball.volleyballcommunitybackend.model.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeNode {
    /** Neo4j 内部 elementId */
    private String elementId;
    /** 业务 ID（UUID） */
    private String id;
    /** 实体名称 */
    private String name;
    /** 实体类型 */
    private String type;
    /** 描述 */
    private String description;
    /** 额外属性（各类型特有属性） */
    private Map<String, Object> properties;
}
