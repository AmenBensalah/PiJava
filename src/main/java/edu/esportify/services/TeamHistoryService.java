package edu.esportify.services;

import edu.esportify.entities.TeamAlert;
import edu.esportify.entities.TeamHistory;
import edu.esportify.navigation.AppSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamHistoryService {
    private static final List<TeamHistory> LOCAL_DATA = new ArrayList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    public List<TeamHistory> getByTeam(int teamId) {
        ensureSeedData(teamId);
        return LOCAL_DATA.stream()
                .filter(history -> history.getTeamId() == teamId)
                .sorted(Comparator.comparing(TeamHistory::getCreatedAt).reversed())
                .toList();
    }

    public void recordAlertAction(TeamAlert alert) {
        if (LOCAL_DATA.stream().anyMatch(item -> item.getTeamId() == alert.getTeamId()
                && safe(item.getDescription()).equals(buildAlertDescription(alert)))) {
            return;
        }
        TeamHistory history = new TeamHistory();
        history.setId(NEXT_ID.getAndIncrement());
        history.setTeamId(alert.getTeamId());
        history.setActionType("ALERTE");
        history.setAuthorUsername(AppSession.getInstance().getUsername());
        history.setDescription(buildAlertDescription(alert));
        LOCAL_DATA.add(history);
    }

    private void ensureSeedData(int teamId) {
        if (teamId <= 0 || LOCAL_DATA.stream().anyMatch(item -> item.getTeamId() == teamId && "SUIVI".equals(item.getActionType()))) {
            return;
        }
        TeamHistory history = new TeamHistory();
        history.setId(NEXT_ID.getAndIncrement());
        history.setTeamId(teamId);
        history.setActionType("SUIVI");
        history.setAuthorUsername("system");
        history.setDescription("Initialisation de l'historique de resolution et de performance.");
        LOCAL_DATA.add(history);
    }

    private String buildAlertDescription(TeamAlert alert) {
        return alert.getTitle() + " | cause: " + safe(alert.getCause()) + " | resolution: " + safe(alert.getResolutionAction());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
