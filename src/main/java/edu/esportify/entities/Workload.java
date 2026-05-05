package edu.esportify.entities;

public class Workload {
    private final String username;
    private final int assignedTasks;
    private final int estimatedHours;
    private final int actualHours;

    public Workload(String username, int assignedTasks, int estimatedHours, int actualHours) {
        this.username = username;
        this.assignedTasks = assignedTasks;
        this.estimatedHours = estimatedHours;
        this.actualHours = actualHours;
    }

    public String getUsername() {
        return username;
    }

    public int getAssignedTasks() {
        return assignedTasks;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public int getActualHours() {
        return actualHours;
    }
}
