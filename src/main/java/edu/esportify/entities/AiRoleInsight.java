package edu.esportify.entities;

import java.util.List;

public class AiRoleInsight {
    private final String roleName;
    private final String badge;
    private final String summary;
    private final String priority;
    private final List<String> recommendations;

    public AiRoleInsight(String roleName, String badge, String summary, String priority, List<String> recommendations) {
        this.roleName = roleName;
        this.badge = badge;
        this.summary = summary;
        this.priority = priority;
        this.recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }

    public String getRoleName() {
        return roleName;
    }

    public String getBadge() {
        return badge;
    }

    public String getSummary() {
        return summary;
    }

    public String getPriority() {
        return priority;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }
}
