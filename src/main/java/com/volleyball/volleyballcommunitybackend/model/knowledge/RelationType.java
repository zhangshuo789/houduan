package com.volleyball.volleyballcommunitybackend.model.knowledge;

public enum RelationType {
    PLAYS_FOR("效力于"),
    PARTICIPATES_IN("参加"),
    BELONGS_TO("属于"),
    COACHES("执教"),
    TEAMMATE_OF("队友");

    private final String label;

    RelationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
