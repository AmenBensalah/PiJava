package edu.esportify.entities;

import java.time.LocalDateTime;

public class TeamAlert {
    private int id;
    private int teamId;
    private Integer taskId;
    private String title;
    private String cause;
    private String resolutionAction;
    private boolean resolved;
    private String notifiedManagerUsername;
    private LocalDateTime createdAt;

    public TeamAlert() {
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getResolutionAction() {
        return resolutionAction;
    }

    public void setResolutionAction(String resolutionAction) {
        this.resolutionAction = resolutionAction;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getNotifiedManagerUsername() {
        return notifiedManagerUsername;
    }

    public void setNotifiedManagerUsername(String notifiedManagerUsername) {
        this.notifiedManagerUsername = notifiedManagerUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
