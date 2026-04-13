package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.ManagerRequest;
import edu.esportify.services.EquipeService;
import edu.esportify.services.ManagerRequestService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminOverviewController implements AdminContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EquipeService equipeService = new EquipeService();
    private final ManagerRequestService managerRequestService = new ManagerRequestService();

    @FXML private Label heroSubtitleLabel;
    @FXML private Label totalTeamsLabel;
    @FXML private Label activeTeamsLabel;
    @FXML private Label privateTeamsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label approvedRequestsLabel;
    @FXML private Label regionsLabel;
    @FXML private PieChart regionChart;
    @FXML private BarChart<String, Number> requestChart;
    @FXML private TableView<Equipe> recentTeamsTable;
    @FXML private TableColumn<Equipe, String> teamNameColumn;
    @FXML private TableColumn<Equipe, String> teamRegionColumn;
    @FXML private TableColumn<Equipe, String> teamManagerColumn;
    @FXML private TableColumn<Equipe, String> teamStatusColumn;
    @FXML private TableView<ManagerRequest> recentRequestsTable;
    @FXML private TableColumn<ManagerRequest, String> requestUserColumn;
    @FXML private TableColumn<ManagerRequest, String> requestEmailColumn;
    @FXML private TableColumn<ManagerRequest, String> requestStatusColumn;
    @FXML private TableColumn<ManagerRequest, String> requestDateColumn;

    @Override
    public void init(AdminLayoutController parentController) {
        configureTables();
        loadData();
    }

    private void configureTables() {
        teamNameColumn.setCellValueFactory(new PropertyValueFactory<>("nomEquipe"));
        teamRegionColumn.setCellValueFactory(new PropertyValueFactory<>("region"));
        teamManagerColumn.setCellValueFactory(new PropertyValueFactory<>("managerUsername"));
        teamStatusColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().isActive() ? "Active" : "Inactive"
        ));

        requestUserColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        requestEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestDateColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(
                () -> cellData.getValue().getCreatedAt() == null ? "-" : cellData.getValue().getCreatedAt().format(DATE_FORMATTER)
        ));
    }

    private void loadData() {
        List<Equipe> equipes = getEquipesSafe();
        List<ManagerRequest> requests = getRequestsSafe();

        long totalTeams = equipes.size();
        long activeTeams = equipes.stream().filter(Equipe::isActive).count();
        long privateTeams = equipes.stream().filter(Equipe::isPrivate).count();
        long pendingRequests = requests.stream().filter(request -> equalsIgnoreCase(request.getStatus(), "En attente")).count();
        long approvedRequests = requests.stream().filter(request -> equalsIgnoreCase(request.getStatus(), "Acceptee")).count();
        long regions = equipes.stream().map(Equipe::getRegion).filter(region -> region != null && !region.isBlank()).distinct().count();

        totalTeamsLabel.setText(String.valueOf(totalTeams));
        activeTeamsLabel.setText(String.valueOf(activeTeams));
        privateTeamsLabel.setText(String.valueOf(privateTeams));
        pendingRequestsLabel.setText(String.valueOf(pendingRequests));
        approvedRequestsLabel.setText(String.valueOf(approvedRequests));
        regionsLabel.setText(String.valueOf(regions));
        heroSubtitleLabel.setText(totalTeams + " groupes suivis et " + requests.size() + " demandes manager gerees depuis le back-office.");

        populateRegionChart(equipes);
        populateRequestChart(requests);

        recentTeamsTable.setItems(FXCollections.observableArrayList(
                equipes.stream()
                        .sorted(Comparator.comparing(Equipe::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(6)
                        .toList()
        ));
        recentRequestsTable.setItems(FXCollections.observableArrayList(
                requests.stream()
                        .sorted(Comparator.comparing(ManagerRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(6)
                        .toList()
        ));
    }

    private void populateRegionChart(List<Equipe> equipes) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Equipe equipe : equipes) {
            String region = equipe.getRegion() == null || equipe.getRegion().isBlank() ? "N/A" : equipe.getRegion();
            counts.put(region, counts.getOrDefault(region, 0) + 1);
        }
        regionChart.setData(FXCollections.observableArrayList(
                counts.entrySet().stream().map(entry -> new PieChart.Data(entry.getKey(), entry.getValue())).toList()
        ));
    }

    private void populateRequestChart(List<ManagerRequest> requests) {
        requestChart.getData().clear();
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("En attente", 0);
        counts.put("Acceptee", 0);
        counts.put("Refusee", 0);
        for (ManagerRequest request : requests) {
            String key = request.getStatus() == null || request.getStatus().isBlank() ? "En attente" : request.getStatus();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        counts.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        requestChart.getData().add(series);
    }

    private boolean equalsIgnoreCase(String left, String right) {
        String normalizedLeft = left == null ? "" : left;
        String normalizedRight = right == null ? "" : right;
        return normalizedLeft.equalsIgnoreCase(normalizedRight);
    }

    private List<Equipe> getEquipesSafe() {
        try {
            List<Equipe> equipes = equipeService.getData();
            if (equipes != null) {
                return equipes;
            }
        } catch (RuntimeException e) {
            System.out.println("Lecture equipes admin impossible, fallback local: " + e.getMessage());
        }
        List<Equipe> fallback = new ArrayList<>();
        fallback.add(createEquipe(1, "Nebula Wolves", "Europe", "manager_alpha", true, false, LocalDateTime.now().minusDays(10)));
        fallback.add(createEquipe(2, "Atlas Vortex", "North America", "manager_beta", true, true, LocalDateTime.now().minusDays(7)));
        fallback.add(createEquipe(3, "Solar Reign", "Asia", "manager_gamma", false, false, LocalDateTime.now().minusDays(3)));
        return fallback;
    }

    private List<ManagerRequest> getRequestsSafe() {
        try {
            List<ManagerRequest> requests = managerRequestService.getData();
            if (requests != null) {
                return requests;
            }
        } catch (RuntimeException e) {
            System.out.println("Lecture demandes admin impossible, fallback local: " + e.getMessage());
        }
        List<ManagerRequest> fallback = new ArrayList<>();
        fallback.add(createRequest(1, "zriga", "rayenborgi@gmail.com", "En attente", LocalDateTime.now().minusDays(2)));
        fallback.add(createRequest(2, "nova.manager", "nova@esportify.gg", "Acceptee", LocalDateTime.now().minusDays(5)));
        fallback.add(createRequest(3, "echo.strat", "echo@esportify.gg", "Refusee", LocalDateTime.now().minusDays(1)));
        return fallback;
    }

    private Equipe createEquipe(int id, String nom, String region, String manager, boolean active, boolean isPrivate, LocalDateTime createdAt) {
        Equipe equipe = new Equipe();
        equipe.setId(id);
        equipe.setNomEquipe(nom);
        equipe.setRegion(region);
        equipe.setManagerUsername(manager);
        equipe.setActive(active);
        equipe.setPrivate(isPrivate);
        equipe.setDateCreation(createdAt);
        return equipe;
    }

    private ManagerRequest createRequest(int id, String username, String email, String status, LocalDateTime createdAt) {
        ManagerRequest request = new ManagerRequest();
        request.setId(id);
        request.setUsername(username);
        request.setEmail(email);
        request.setStatus(status);
        request.setCreatedAt(createdAt);
        request.setMotivation("Demande manager generee en mode local.");
        return request;
    }
}
