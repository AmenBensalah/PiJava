package edu.esportify.services;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Task;
import edu.esportify.entities.TaskStatus;
import edu.esportify.entities.TeamMember;
import edu.esportify.entities.TeamPerformance;
import edu.esportify.entities.Workload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TeamPerformanceService {
    private final TaskService taskService = new TaskService();
    private final CandidatureService candidatureService = new CandidatureService();

    public TeamPerformance calculatePerformance(int teamId) {
        List<Task> tasks = taskService.getByTeam(teamId);
        if (tasks.isEmpty()) {
            return new TeamPerformance(0, 0, 0, 0, 0);
        }

        long completed = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        long blocked = tasks.stream().filter(task -> task.getStatus() == TaskStatus.BLOCKED).count();
        int delayed = taskService.getDelayedTasks(teamId).size();
        long onTimeCompleted = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .filter(task -> task.getCompletedAt() != null && task.getDueDate() != null && !task.getCompletedAt().isAfter(task.getDueDate()))
                .count();

        double completionRate = percentage(completed, tasks.size());
        double deadlineRespectRate = completed == 0 ? 0 : percentage(onTimeCompleted, completed);
        int estimatedHours = tasks.stream().mapToInt(Task::getEstimatedHours).sum();
        int actualHours = tasks.stream().mapToInt(Task::getActualHours).sum();
        double productivity = estimatedHours == 0 ? 0 : Math.max(0, 100 - (((double) Math.max(0, actualHours - estimatedHours) / estimatedHours) * 100));

        return new TeamPerformance(completionRate, deadlineRespectRate, productivity, delayed, (int) blocked);
    }

    public List<Workload> getWorkloads(int teamId, String managerUsername) {
        Map<String, WorkloadAccumulator> accumulators = new LinkedHashMap<>();
        for (Task task : taskService.getByTeam(teamId)) {
            String username = safe(task.getAssigneeUsername(), managerUsername);
            WorkloadAccumulator accumulator = accumulators.computeIfAbsent(username, key -> new WorkloadAccumulator());
            accumulator.assignedTasks++;
            accumulator.estimatedHours += task.getEstimatedHours();
            accumulator.actualHours += task.getActualHours();
        }
        List<Workload> workloads = new ArrayList<>();
        accumulators.forEach((username, value) ->
                workloads.add(new Workload(username, value.assignedTasks, value.estimatedHours, value.actualHours)));
        return workloads;
    }

    public List<TeamMember> getActiveMembers(int teamId, String managerUsername) {
        List<TeamMember> members = new ArrayList<>();
        members.add(new TeamMember(managerUsername, "MANAGER", true));
        for (Candidature candidature : candidatureService.getAcceptedMembersByEquipe(teamId)) {
            members.add(new TeamMember(candidature.getPseudoJoueur(), candidature.getNiveau(), true));
        }
        return members;
    }

    private double percentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return (numerator * 100.0) / denominator;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static final class WorkloadAccumulator {
        private int assignedTasks;
        private int estimatedHours;
        private int actualHours;
    }
}
