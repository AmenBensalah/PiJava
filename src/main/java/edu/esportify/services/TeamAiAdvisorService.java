package edu.esportify.services;

import edu.esportify.entities.AiRoleInsight;
import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.Task;
import edu.esportify.entities.TaskStatus;
import edu.esportify.entities.TeamAlert;
import edu.esportify.entities.TeamMember;
import edu.esportify.entities.TeamPerformance;
import edu.esportify.entities.Workload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class
TeamAiAdvisorService {
    private final TeamPerformanceService teamPerformanceService = new TeamPerformanceService();
    private final TaskService taskService = new TaskService();
    private final TeamAlertService teamAlertService = new TeamAlertService();
    private final CandidatureService candidatureService = new CandidatureService();
    private final RecrutementService recrutementService = new RecrutementService();

    public List<AiRoleInsight> buildInsights(Equipe equipe, String managerUsername) {
        if (equipe == null || equipe.getId() <= 0) {
            return List.of();
        }

        TeamPerformance performance = teamPerformanceService.calculatePerformance(equipe.getId());
        List<Task> tasks = taskService.getByTeam(equipe.getId());
        List<Task> delayedTasks = taskService.getDelayedTasks(equipe.getId());
        List<TeamAlert> alerts = teamAlertService.getByTeam(equipe.getId());
        List<Candidature> candidatures = candidatureService.getByEquipe(equipe.getId());
        List<Candidature> accepted = candidatureService.getAcceptedMembersByEquipe(equipe.getId());
        List<TeamMember> members = teamPerformanceService.getActiveMembers(equipe.getId(), managerUsername);
        List<Workload> workloads = teamPerformanceService.getWorkloads(equipe.getId(), managerUsername);

        List<AiRoleInsight> insights = new ArrayList<>();
        insights.add(buildAnalystInsight(equipe, performance, tasks, delayedTasks, members, workloads));
        insights.add(buildRecruiterInsight(equipe, candidatures, accepted));
        insights.add(buildCoachInsight(tasks, performance, accepted));
        insights.add(buildPlannerInsight(tasks, delayedTasks, workloads));
        insights.add(buildCommunityInsight(equipe, candidatures));
        insights.add(buildModeratorInsight(alerts, candidatures, tasks));
        return insights;
    }

    private AiRoleInsight buildAnalystInsight(Equipe equipe, TeamPerformance performance, List<Task> tasks,
                                              List<Task> delayedTasks, List<TeamMember> members, List<Workload> workloads) {
        String force = performance.getProductivityScore() >= 70
                ? "bonne productivite"
                : "marge de progression sur l'execution";
        String regularity = delayedTasks.isEmpty() ? "bonne regularite" : delayedTasks.size() + " retard(s) detecte(s)";
        String summary = "L'equipe " + safe(equipe.getNomEquipe()) + " montre une " + force
                + ", avec " + formatPercent(performance.getTaskCompletionRate()) + " de taches completes et une "
                + regularity + ".";

        List<String> recommendations = new ArrayList<>();
        recommendations.add("Conserver les points forts sur les taches deja maitrisees: " + completedTasks(tasks) + " tache(s) terminee(s).");
        recommendations.add(delayedTasks.isEmpty()
                ? "Maintenir le rythme avec un point hebdomadaire de suivi."
                : "Traiter en priorite les retards pour retrouver une execution stable.");
        recommendations.add(workloads.isEmpty()
                ? "Aucune charge detectee: assigner des objectifs mesurables aux membres."
                : "Reequilibrer la charge entre les profils les plus sollicites et les moins engages.");

        return new AiRoleInsight("Analyste de performance IA", "Performance", summary,
                performance.getProductivityScore() >= 70 ? "Stable" : "A surveiller", recommendations);
    }

    private AiRoleInsight buildRecruiterInsight(Equipe equipe, List<Candidature> candidatures, List<Candidature> accepted) {
        int occupiedSlots = accepted.size() + 1;
        int openSlots = Math.max(0, equipe.getMaxMembers() - occupiedSlots);
        Map<String, Long> availabilityCounts = accepted.stream()
                .collect(Collectors.groupingBy(item -> safe(item.getDisponibilite()).toUpperCase(), LinkedHashMap::new, Collectors.counting()));
        String priorityAvailability = availabilityCounts.entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("SOIR");

        String summary = candidatures.isEmpty()
                ? "Aucun profil en attente. L'equipe dispose de " + openSlots + " place(s) libre(s) a combler."
                : candidatures.size() + " candidature(s) a comparer pour " + openSlots + " place(s) restante(s), avec une priorite probable sur la disponibilite " + priorityAvailability + ".";

        List<String> recommendations = new ArrayList<>();
        recommendations.add(candidatures.isEmpty()
                ? "Publier une annonce ciblee avec niveau et disponibilite attendus."
                : "Prioriser les candidats dont la disponibilite correspond le mieux au rythme actuel du roster.");
        recommendations.add("Verifier la compatibilite regionale et les disponibilites avant validation finale.");
        recommendations.add(topCandidateTip(candidatures, priorityAvailability));

        return new AiRoleInsight("Recruteur IA", "Recrutement", summary, openSlots > 0 ? "Action" : "Complet", recommendations);
    }

    private AiRoleInsight buildCoachInsight(List<Task> tasks, TeamPerformance performance, List<Candidature> accepted) {
        long analysisTasks = tasks.stream().filter(task -> containsOneOf(task.getTitle(), "analyse", "vod", "adversaire")).count();
        long blockedTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.BLOCKED).count();
        String style = accepted.size() >= 3 ? "roster structure" : "roster encore en construction";
        String summary = "Le coach tactique detecte un " + style + ", avec " + analysisTasks
                + " tache(s) liee(s) a l'analyse de jeu et " + blockedTasks + " point(s) tactique(s) bloque(s).";

        List<String> recommendations = new ArrayList<>();
        recommendations.add("Prevoir une review VOD avant chaque scrim important pour accelerer l'adaptation.");
        recommendations.add(performance.getBlockedTasksCount() > 0
                ? "Lever les blocages tactiques avant de lancer un nouveau plan de match."
                : "Capitaliser sur la dynamique actuelle avec une composition standard puis une variante surprise.");
        recommendations.add("Definir un plan A agressif et un plan B plus safe selon le niveau de l'adversaire.");

        return new AiRoleInsight("Coach tactique IA", "Strategie", summary,
                performance.getBlockedTasksCount() > 0 ? "Ajustement" : "Pret", recommendations);
    }

    private AiRoleInsight buildPlannerInsight(List<Task> tasks, List<Task> delayedTasks, List<Workload> workloads) {
        long todoTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.TODO).count();
        Workload heaviest = workloads.stream().max(Comparator.comparingInt(Workload::getAssignedTasks)).orElse(null);
        String summary = "Le planning recense " + tasks.size() + " tache(s), dont " + todoTasks
                + " a planifier et " + delayedTasks.size() + " en retard.";

        List<String> recommendations = new ArrayList<>();
        recommendations.add("Bloquer des creneaux fixes pour scrims, review et reunion staff chaque semaine.");
        recommendations.add(delayedTasks.isEmpty()
                ? "Le calendrier est sain: garder une marge de preparation avant les echeances."
                : "Replanifier en priorite les taches en retard dans les 48 prochaines heures.");
        recommendations.add(heaviest == null
                ? "Assigner clairement les responsables des prochaines actions."
                : "Alleger la charge de " + heaviest.getUsername() + " qui porte actuellement le plus de taches.");

        return new AiRoleInsight("Assistant planning IA", "Planning", summary,
                delayedTasks.isEmpty() ? "Fluide" : "Charge", recommendations);
    }

    private AiRoleInsight buildCommunityInsight(Equipe equipe, List<Candidature> candidatures) {
        int openRecruitments = recrutementService.getByEquipe(equipe.getId()).size();
        String summary = "La communication de " + safe(equipe.getNomEquipe()) + " peut s'appuyer sur "
                + openRecruitments + " annonce(s) active(s) et " + candidatures.size() + " interaction(s) candidat(s).";

        List<String> recommendations = new ArrayList<>();
        recommendations.add("Publier une actu hebdomadaire sur la vie du roster et les objectifs du moment.");
        recommendations.add(openRecruitments > 0
                ? "Transformer chaque recrutement ouvert en post reseaux avec niveau, disponibilite et lien Discord."
                : "Relancer la visibilite avec une presentation d'equipe et les prochains objectifs competitifs.");
        recommendations.add("Mettre en avant les valeurs de l'equipe pour attirer des profils compatibles.");

        return new AiRoleInsight("Community manager IA", "Communication", summary, "Visibilite", recommendations);
    }

    private AiRoleInsight buildModeratorInsight(List<TeamAlert> alerts, List<Candidature> candidatures, List<Task> tasks) {
        long blockedTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.BLOCKED).count();
        long pendingCandidates = candidatures.stream().filter(item -> "En attente".equalsIgnoreCase(safe(item.getStatut()))).count();
        String summary = "Le climat d'equipe presente " + alerts.size() + " alerte(s), "
                + blockedTasks + " blocage(s) et " + pendingCandidates + " candidature(s) encore sans decision.";

        List<String> recommendations = new ArrayList<>();
        recommendations.add(alerts.isEmpty()
                ? "Aucun signal critique: conserver des points de feedback reguliers."
                : "Traiter rapidement les alertes pour eviter qu'un conflit operationnel ne s'installe.");
        recommendations.add(pendingCandidates > 2
                ? "Reduire le temps de reponse aux candidatures pour limiter la frustration externe."
                : "Maintenir une communication claire sur les decisions de recrutement.");
        recommendations.add("Formaliser une charte de comportement et un canal de signalement interne.");

        return new AiRoleInsight("Moderateur IA", "Climat", summary,
                alerts.isEmpty() ? "Sain" : "Vigilance", recommendations);
    }

    private boolean containsOneOf(String value, String... keywords) {
        String normalized = safe(value).toLowerCase();
        for (String keyword : keywords) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private long completedTasks(List<Task> tasks) {
        return tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
    }

    private String topCandidateTip(List<Candidature> candidatures, String preferredAvailability) {
        return candidatures.stream()
                .sorted(Comparator
                        .comparing((Candidature item) -> !preferredAvailability.equalsIgnoreCase(safe(item.getDisponibilite())))
                        .thenComparing(item -> rankLevel(safe(item.getNiveau()))))
                .findFirst()
                .map(item -> "Profil a etudier en priorite: " + safe(item.getPseudoJoueur()) + " (" + safe(item.getNiveau()) + " - " + safe(item.getDisponibilite()) + ").")
                .orElse("Aucun profil n'est encore assez renseigne pour une priorisation automatique.");
    }

    private int rankLevel(String level) {
        String normalized = level.toLowerCase();
        if (normalized.contains("radiant") || normalized.contains("challenger")) {
            return 0;
        }
        if (normalized.contains("grandmaster")) {
            return 0;
        }
        if (normalized.contains("legend")) {
            return 1;
        }
        if (normalized.contains("heroique")) {
            return 2;
        }
        if (normalized.contains("diamenet") || normalized.contains("diamond")) {
            return 3;
        }
        if (normalized.contains("gold")) {
            return 4;
        }
        if (normalized.contains("silver")) {
            return 5;
        }
        if (normalized.contains("bronze")) {
            return 6;
        }
        if (normalized.contains("amateur")) {
            return 7;
        }
        if (normalized.contains("immortal") || normalized.contains("master")) {
            return 1;
        }
        return 8;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatPercent(double value) {
        return String.format("%.0f%%", value);
    }
}
