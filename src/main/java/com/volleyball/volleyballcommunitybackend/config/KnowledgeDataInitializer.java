package com.volleyball.volleyballcommunitybackend.config;

import com.volleyball.volleyballcommunitybackend.model.knowledge.EntityType;
import com.volleyball.volleyballcommunitybackend.model.knowledge.RelationType;
import com.volleyball.volleyballcommunitybackend.service.KnowledgeService;
import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 首次启动时自动初始化中国女排示例数据
 */
@Component
public class KnowledgeDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDataInitializer.class);

    private final KnowledgeService knowledgeService;
    private final Driver neo4jDriver;

    /** 初始化标记，防止重复插入 */
    private static boolean initialized = false;

    public KnowledgeDataInitializer(KnowledgeService knowledgeService, Driver neo4jDriver) {
        this.knowledgeService = knowledgeService;
        this.neo4jDriver = neo4jDriver;
    }

    @Override
    public void run(String... args) {
        if (initialized) {
            return;
        }
        try {
            // 检查是否已有数据
            var count = countEntities();
            if (count > 0) {
                log.info("知识图谱已有 {} 个实体，跳过初始化", count);
                initialized = true;
                return;
            }

            log.info("开始初始化排球知识图谱示例数据...");
            initData();
            initialized = true;
            log.info("排球知识图谱示例数据初始化完成");
        } catch (Exception e) {
            log.error("知识图谱数据初始化失败: {}", e.getMessage());
        }
    }

    private long countEntities() {
        try (var session = neo4jDriver.session()) {
            var result = session.run("MATCH (n) RETURN count(n) AS cnt");
            return result.single().get("cnt").asLong();
        } catch (Exception e) {
            return 0;
        }
    }

    private void initData() {
        // ==================== 球队 ====================
        var chinaTeam = knowledgeService.createEntity("中国女排", EntityType.TEAM,
                "中国国家女子排球队，世界排坛传统强队，多次获得世界冠军",
                Map.of("country", "中国", "teamType", "国家队"),
                null);

        var brazilTeam = knowledgeService.createEntity("巴西女排", EntityType.TEAM,
                "巴西国家女子排球队，南美劲旅",
                Map.of("country", "巴西", "teamType", "国家队"),
                null);

        var usaTeam = knowledgeService.createEntity("美国女排", EntityType.TEAM,
                "美国国家女子排球队",
                Map.of("country", "美国", "teamType", "国家队"),
                null);

        var tianjinTeam = knowledgeService.createEntity("天津渤海银行女排", EntityType.TEAM,
                "中国排球超级联赛俱乐部，多次获得联赛冠军",
                Map.of("country", "中国", "teamType", "俱乐部"),
                null);

        // ==================== 球员 ====================
        var zhuting = knowledgeService.createEntity("朱婷", EntityType.PLAYER,
                "中国女排队长，世界顶级主攻手，被誉为MVP收割机",
                Map.of("position", "主攻", "height", "198cm", "nationality", "中国",
                       "birthDate", "1994-11-29"),
                null);

        var yuanxinyue = knowledgeService.createEntity("袁心玥", EntityType.PLAYER,
                "中国女排副攻手，身高超2米，网前威慑力极强",
                Map.of("position", "副攻", "height", "201cm", "nationality", "中国",
                       "birthDate", "1996-12-21"),
                null);

        var zhangchangning = knowledgeService.createEntity("张常宁", EntityType.PLAYER,
                "中国女排主攻手，技术全面，一传和进攻能力出众",
                Map.of("position", "主攻", "height", "195cm", "nationality", "中国",
                       "birthDate", "1995-11-06"),
                null);

        var gongxiangyu = knowledgeService.createEntity("龚翔宇", EntityType.PLAYER,
                "中国女排接应，技术细腻，防守和发球出色",
                Map.of("position", "接应", "height", "186cm", "nationality", "中国",
                       "birthDate", "1997-04-21"),
                null);

        var langping = knowledgeService.createEntity("郎平", EntityType.PLAYER,
                "前中国女排主攻手，后担任中国女排主教练，带领中国女排重返巅峰",
                Map.of("position", "主攻（退役）", "height", "184cm", "nationality", "中国",
                       "birthDate", "1960-12-10"),
                null);

        // ==================== 赛事 ====================
        var olympics2024 = knowledgeService.createEntity("2024巴黎奥运会", EntityType.TOURNAMENT,
                "第33届夏季奥林匹克运动会排球比赛",
                Map.of("year", 2024, "tournamentType", "奥运会"),
                null);

        var worldChampionship = knowledgeService.createEntity("2022女排世锦赛", EntityType.TOURNAMENT,
                "2022年世界女子排球锦标赛",
                Map.of("year", 2022, "tournamentType", "世锦赛"),
                null);

        var worldCup2019 = knowledgeService.createEntity("2019女排世界杯", EntityType.TOURNAMENT,
                "2019年女排世界杯，中国女排以11连胜夺冠",
                Map.of("year", 2019, "tournamentType", "世界杯"),
                null);

        // ==================== 比赛 ====================
        var match1 = knowledgeService.createEntity("奥运会女排决赛", EntityType.MATCH,
                "2024巴黎奥运会女排决赛，中国女排获得金牌",
                Map.of("matchDate", "2024-08-11", "location", "巴黎", "result", "中国女排 3-0 获胜"),
                null);

        var match2 = knowledgeService.createEntity("世界杯中国vs巴西", EntityType.MATCH,
                "2019世界杯中国女排对阵巴西女排，经典对决",
                Map.of("matchDate", "2019-09-22", "location", "日本大阪", "result", "中国女排 3-2 获胜"),
                null);

        var match3 = knowledgeService.createEntity("世锦赛中国vs美国", EntityType.MATCH,
                "2022世锦赛中国女排对阵美国女排",
                Map.of("matchDate", "2022-10-05", "location", "荷兰", "result", "中国女排 3-1 获胜"),
                null);

        // ==================== 关系 ====================
        // 球员 → 球队
        knowledgeService.createRelation(zhuting.getId(), chinaTeam.getId(), RelationType.PLAYS_FOR,
                Map.of("number", "2"));
        knowledgeService.createRelation(yuanxinyue.getId(), chinaTeam.getId(), RelationType.PLAYS_FOR,
                Map.of("number", "1"));
        knowledgeService.createRelation(zhangchangning.getId(), chinaTeam.getId(), RelationType.PLAYS_FOR,
                Map.of("number", "9"));
        knowledgeService.createRelation(gongxiangyu.getId(), chinaTeam.getId(), RelationType.PLAYS_FOR,
                Map.of("number", "6"));
        knowledgeService.createRelation(zhuting.getId(), tianjinTeam.getId(), RelationType.PLAYS_FOR,
                Map.of("number", "2"));

        // 教练 → 球队
        knowledgeService.createRelation(langping.getId(), chinaTeam.getId(), RelationType.COACHES,
                Map.of("since", "2013", "until", "2021"));

        // 队友关系
        knowledgeService.createRelation(zhuting.getId(), yuanxinyue.getId(), RelationType.TEAMMATE_OF,
                Map.of("team", "中国女排"));
        knowledgeService.createRelation(zhuting.getId(), zhangchangning.getId(), RelationType.TEAMMATE_OF,
                Map.of("team", "中国女排"));
        knowledgeService.createRelation(zhuting.getId(), gongxiangyu.getId(), RelationType.TEAMMATE_OF,
                Map.of("team", "中国女排"));
        knowledgeService.createRelation(yuanxinyue.getId(), zhangchangning.getId(), RelationType.TEAMMATE_OF,
                Map.of("team", "中国女排"));
        knowledgeService.createRelation(yuanxinyue.getId(), gongxiangyu.getId(), RelationType.TEAMMATE_OF,
                Map.of("team", "中国女排"));
        knowledgeService.createRelation(zhangchangning.getId(), gongxiangyu.getId(), RelationType.TEAMMATE_OF,
                Map.of("team", "中国女排"));

        // 球队 → 比赛
        knowledgeService.createRelation(chinaTeam.getId(), match1.getId(), RelationType.PARTICIPATES_IN, null);
        knowledgeService.createRelation(chinaTeam.getId(), match2.getId(), RelationType.PARTICIPATES_IN, null);
        knowledgeService.createRelation(chinaTeam.getId(), match3.getId(), RelationType.PARTICIPATES_IN, null);
        knowledgeService.createRelation(brazilTeam.getId(), match2.getId(), RelationType.PARTICIPATES_IN, null);
        knowledgeService.createRelation(usaTeam.getId(), match3.getId(), RelationType.PARTICIPATES_IN, null);

        // 比赛 → 赛事
        knowledgeService.createRelation(match1.getId(), olympics2024.getId(), RelationType.BELONGS_TO, null);
        knowledgeService.createRelation(match2.getId(), worldCup2019.getId(), RelationType.BELONGS_TO, null);
        knowledgeService.createRelation(match3.getId(), worldChampionship.getId(), RelationType.BELONGS_TO, null);

        log.info("已创建 {} 个实体和 {} 条关系",
                countEntities(), countRelations());
    }

    private long countRelations() {
        try (var session = neo4jDriver.session()) {
            var result = session.run("MATCH ()-[r]->() RETURN count(r) AS cnt");
            return result.single().get("cnt").asLong();
        } catch (Exception e) {
            return 0;
        }
    }
}
