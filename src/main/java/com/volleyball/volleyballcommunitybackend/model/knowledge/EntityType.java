package com.volleyball.volleyballcommunitybackend.model.knowledge;

public enum EntityType {
    PLAYER("球员"),
    TEAM("球队"),
    MATCH("比赛"),
    TOURNAMENT("赛事");

    private final String label;

    EntityType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
