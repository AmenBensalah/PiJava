package edu.esportify.services;

import edu.esportify.entities.Task;
import edu.esportify.entities.TaskStatus;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.TeamAlert;
import edu.esportify.navigation.AppSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamAlertService {
    private static final int BAN_THRESHOLD = 5;
    private static final String TEAM_REPORT_TITLE = "Signalement equipe";
    private static final String ADMIN_ESCALATION_TITLE = "Seuil de bannissement atteint";
    private static final List<TeamAlert> LOCAL_DATA = new ArrayList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    public List<TeamAlert> getByTeam(int teamId) {
        ensureSeedData(teamId);
        return LOCAL_DATA.stream()
                .filter(alert -> alert.getTeamId() == teamId)
                .sorted(Comparator.comparing(TeamAlert::getCreatedAt).reversed())
                .toList();
    }

    public void signalBlockedTask(int teamId, Task task, String cause, String resolutionAction) {
        TeamAlert alert = new TeamAlert();
        alert.setId(NEXT_ID.getAndIncrement());
        alert.setTeamId(teamId);
        alert.setTaskId(task == null ? null : task.getId());
        alert.setTitle(task == null ? "Blocage signale" : "Tache bloquee: " + task.getTitle());
        alert.setCause(cause);
        alert.setResolutionAction(resolutionAction);
        alert.setResolved(false);
        alert.setNotifiedManagerUsername(AppSession.getInstance().getUsername());
        LOCAL_DATA.add(alert);
    }

    public boolean hasUserReportedTeam(int teamId, String reporterUsername) {
        String normalized = safe(reporterUsername);
        return LOCAL_DATA.stream()
                .anyMatch(alert -> alert.getTeamId() == teamId
                        && TEAM_REPORT_TITLE.equalsIgnoreCase(safe(alert.getTitle()))
                        && normalized.equalsIgnoreCase(extractReporterUsername(alert)));
    }

    public int countBanReports(int teamId) {
        return (int) LOCAL_DATA.stream()
                .filter(alert -> alert.getTeamId() == teamId)
                .filter(alert -> TEAM_REPORT_TITLE.equalsIgnoreCase(safe(alert.getTitle())))
                .count();
    }

    public void reportTeamForBan(Equipe equipe, String cause, String reporterRole) {
        if (equipe == null || equipe.getId() <= 0) {
            return;
        }
        String reporterUsername = safe(AppSession.getInstance().getUsername());
        if (reporterUsername.isBlank() || hasUserReportedTeam(equipe.getId(), reporterUsername)) {
            return;
        }

        TeamAlert report = new TeamAlert();
        report.setId(NEXT_ID.getAndIncrement());
        report.setTeamId(equipe.getId());
        report.setTitle(TEAM_REPORT_TITLE);
        report.setCause(safe(cause));
        report.setResolutionAction("Reporte par " + reporterUsername + " (" + safe(reporterRole) + ")");
        report.setResolved(false);
        report.setNotifiedManagerUsername(reporterUsername);
        LOCAL_DATA.add(report);

        if (countBanReports(equipe.getId()) >= BAN_THRESHOLD && !hasEscalationAlert(equipe.getId())) {
            TeamAlert escalation = new TeamAlert();
            escalation.setId(NEXT_ID.getAndIncrement());
            escalation.setTeamId(equipe.getId());
            escalation.setTitle(ADMIN_ESCALATION_TITLE);
            escalation.setCause("Plus de " + BAN_THRESHOLD + " signalements recus pour l'equipe " + safe(equipe.getNomEquipe()) + ".");
            escalation.setResolutionAction("Verifier la team et envisager un bannissement admin.");
            escalation.setResolved(false);
            escalation.setNotifiedManagerUsername("admin");
            LOCAL_DATA.add(escalation);
        }
    }

    private boolean hasEscalationAlert(int teamId) {
        return LOCAL_DATA.stream()
                .anyMatch(alert -> alert.getTeamId() == teamId
                        && ADMIN_ESCALATION_TITLE.equalsIgnoreCase(safe(alert.getTitle())));
    }

    private String extractReporterUsername(TeamAlert alert) {
        String resolution = safe(alert.getResolutionAction());
        if (resolution.startsWith("Reporte par ")) {
            String remainder = resolution.substring("Reporte par ".length());
            int separator = remainder.indexOf(" (");
            return separator >= 0 ? remainder.substring(0, separator).trim() : remainder.trim();
        }
        return safe(alert.getNotifiedManagerUsername());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void ensureSeedData(int teamId) {
        if (teamId <= 0 || LOCAL_DATA.stream().anyMatch(alert -> alert.getTeamId() == teamId)) {
            return;
        }
        TaskService taskService = new TaskService();
        taskService.getByTeam(teamId).stream()
                .filter(task -> task.getStatus() == TaskStatus.BLOCKED)
                .findFirst()
                .ifPresent(task -> signalBlockedTask(
                        teamId,
                        task,
                        "Validation externe en attente",
                        "Responsable notifie et relance programmee"
                ));
    }
}
