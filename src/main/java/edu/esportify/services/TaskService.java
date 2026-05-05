package edu.esportify.services;

import edu.esportify.entities.Task;
import edu.esportify.entities.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskService {
    private static final List<Task> LOCAL_DATA = new ArrayList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    public List<Task> getByTeam(int teamId) {
        ensureSeedData(teamId);
        return LOCAL_DATA.stream()
                .filter(task -> task.getTeamId() == teamId)
                .sorted(Comparator.comparing(Task::getDueDate))
                .toList();
    }

    public List<Task> getDelayedTasks(int teamId) {
        LocalDate today = LocalDate.now();
        return getByTeam(teamId).stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .toList();
    }

    private void add(Task task) {
        if (task.getId() <= 0) {
            task.setId(NEXT_ID.getAndIncrement());
        }
        LOCAL_DATA.removeIf(item -> item.getId() == task.getId());
        LOCAL_DATA.add(task);
    }

    private void ensureSeedData(int teamId) {
        if (teamId <= 0 || LOCAL_DATA.stream().anyMatch(task -> task.getTeamId() == teamId)) {
            return;
        }
        add(createTask(teamId, "Preparation scrim", "Planning des entrainements", "manager", TaskStatus.DONE, -2, -1, 4, 3));
        add(createTask(teamId, "Analyse adversaire", "Review des VOD et points faibles", "nova.user", TaskStatus.IN_PROGRESS, 2, null, 6, 2));
        add(createTask(teamId, "Mise a jour roster", "Validation des disponibilites", "manager", TaskStatus.BLOCKED, 1, null, 3, 1));
        add(createTask(teamId, "Publication recrutement", "Poster l'annonce du mois", "manager", TaskStatus.TODO, -1, null, 2, 0));
    }

    private Task createTask(int teamId, String title, String description, String assignee, TaskStatus status,
                            int dueOffsetDays, Integer completedOffsetDays, int estimatedHours, int actualHours) {
        Task task = new Task();
        task.setTeamId(teamId);
        task.setTitle(title);
        task.setDescription(description);
        task.setAssigneeUsername(assignee);
        task.setStatus(status);
        task.setDueDate(LocalDate.now().plusDays(dueOffsetDays));
        task.setEstimatedHours(estimatedHours);
        task.setActualHours(actualHours);
        if (completedOffsetDays != null) {
            task.setCompletedAt(LocalDate.now().plusDays(completedOffsetDays));
        }
        return task;
    }
}
