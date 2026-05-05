package edu.esportify.entities;

public class TeamPerformance {
    private final double taskCompletionRate;
    private final double deadlineRespectRate;
    private final double productivityScore;
    private final int delayedTasksCount;
    private final int blockedTasksCount;

    public TeamPerformance(double taskCompletionRate, double deadlineRespectRate, double productivityScore,
                           int delayedTasksCount, int blockedTasksCount) {
        this.taskCompletionRate = taskCompletionRate;
        this.deadlineRespectRate = deadlineRespectRate;
        this.productivityScore = productivityScore;
        this.delayedTasksCount = delayedTasksCount;
        this.blockedTasksCount = blockedTasksCount;
    }

    public double getTaskCompletionRate() {
        return taskCompletionRate;
    }

    public double getDeadlineRespectRate() {
        return deadlineRespectRate;
    }

    public double getProductivityScore() {
        return productivityScore;
    }

    public int getDelayedTasksCount() {
        return delayedTasksCount;
    }

    public int getBlockedTasksCount() {
        return blockedTasksCount;
    }
}
