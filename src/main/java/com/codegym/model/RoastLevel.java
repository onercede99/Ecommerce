package com.codegym.model;

public enum RoastLevel {
    LIGHT("Rang Sáng"),
    MEDIUM("Rang Vừa"),
    DARK("Rang Đậm");

    private final String displayName;

    RoastLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}