package edu.esportify.services;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TeamBanNotificationService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CandidatureService candidatureService = new CandidatureService();
    private final UserService userService = new UserService();
    private final BrevoEmailService brevoEmailService = new BrevoEmailService();

    public boolean isEmailConfigured() {
        return brevoEmailService.isConfigured();
    }

    public int notifyTeamBan(Equipe equipe, String reportReason, String reportDetails, String adminUsername) {
        if (equipe == null) {
            return 0;
        }
        List<String> recipients = resolveRecipients(equipe);
        if (recipients.isEmpty()) {
            return 0;
        }

        String reportBody = buildReport(equipe, reportReason, reportDetails, adminUsername);
        brevoEmailService.sendTeamBanReport(recipients, equipe.getNomEquipe(), reportBody);
        return recipients.size();
    }

    private List<String> resolveRecipients(Equipe equipe) {
        Set<String> emails = new LinkedHashSet<>();

        User manager = userService.findByUsername(equipe.getManagerUsername());
        if (manager != null && manager.getEmail() != null && !manager.getEmail().isBlank()) {
            emails.add(manager.getEmail().trim());
        }

        for (Candidature candidature : candidatureService.getAcceptedMembersByEquipe(equipe.getId())) {
            User member = userService.findByUsername(candidature.getAccountUsername());
            if (member != null && member.getEmail() != null && !member.getEmail().isBlank()) {
                emails.add(member.getEmail().trim());
            }
        }

        return List.copyOf(emails);
    }

    private String buildReport(Equipe equipe, String reportReason, String reportDetails, String adminUsername) {
        String reason = safe(reportReason);
        String details = safe(reportDetails);
        StringBuilder builder = new StringBuilder();
        builder.append("Equipe: ").append(safe(equipe.getNomEquipe())).append('\n');
        builder.append("Tag: ").append(safe(equipe.getTag())).append('\n');
        builder.append("Manager: ").append(safe(equipe.getManagerUsername())).append('\n');
        builder.append("Admin responsable: ").append(safe(adminUsername)).append('\n');
        builder.append("Date: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append('\n');
        builder.append('\n');
        builder.append("Motif principal:\n").append(reason).append('\n');
        if (!details.isBlank()) {
            builder.append('\n');
            builder.append("Details du rapport:\n").append(details).append('\n');
        }
        return builder.toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
