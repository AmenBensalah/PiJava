package edu.PROJETPI;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.services.CheckoutService;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.entities.User;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeliveryController implements Initializable {

    @FXML
    private TextField paysField;
    @FXML
    private TextField gouvernoratField;
    @FXML
    private TextField codePostalField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextArea adresseDetailArea;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label totalSummaryLabel;
    @FXML
    private Button continueButton;

    private final CheckoutService checkoutService = new CheckoutService();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private static final int AUTO_CLIENT_ID_PLACEHOLDER = 0;
    private static final String NOMINATIM_REVERSE_URL =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=fr&lat=%s&lon=%s&addressdetails=1";

    private SelectedPlace selectedPlace;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSummary();
        if (OrderSession.getInstance().getCheckoutMode() == OrderSession.CheckoutMode.CASH_ON_DELIVERY) {
            continueButton.setText("Valider paiement a la livraison");
        } else {
            continueButton.setText("Continuer vers paiement");
        }
    }

    @FXML
    private void goBackToCommande() {
        SceneNavigator.switchScene(paysField, "/commande-view.fxml", "Informations commande");
    }

    @FXML
    private void openMapPicker() {
        Stage mapStage = new Stage();
        mapStage.setTitle("Choisir le pays sur la carte");
        mapStage.initModality(Modality.APPLICATION_MODAL);

        Label statusLabel = new Label("Clique sur la carte pour placer le marqueur.");
        statusLabel.getStyleClass().add("map-status");

        Button confirmButton = new Button("Valider la position");
        confirmButton.getStyleClass().add("primary-button");
        confirmButton.setDisable(true);

        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> mapStage.close());

        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        WebEngine webEngine = webView.getEngine();

        MapBridge bridge = new MapBridge(statusLabel, confirmButton);
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", bridge);
            }
        });
        webEngine.loadContent(buildMapHtml());

        confirmButton.setOnAction(event -> {
            if (bridge.place == null) {
                AlertUtils.showError("Choisissez une position sur la carte.");
                return;
            }

            applySelectedPlace(bridge.place);
            mapStage.close();
        });

        Region spacer = new Region();
        HBox actions = new HBox(12, statusLabel, spacer, cancelButton, confirmButton);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        actions.setPadding(new Insets(14));
        actions.setStyle("-fx-background-color: #11182f;");

        BorderPane root = new BorderPane(webView);
        root.setBottom(actions);
        Scene scene = new Scene(root, 920, 620);
        URL stylesheet = getClass().getResource("/styles/esportify-theme.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        mapStage.setScene(scene);
        mapStage.showAndWait();
    }

    @FXML
    private void continueToPayment() {
        Commande commande = readForm();
        if (commande == null) {
            return;
        }

        OrderSession.getInstance().setDraftCommande(commande);

        if (OrderSession.getInstance().getCheckoutMode() == OrderSession.CheckoutMode.CASH_ON_DELIVERY) {
            try {
                int commandeId = checkoutService.checkoutCashOnDelivery(
                        OrderSession.getInstance(),
                        java.sql.Date.valueOf(java.time.LocalDate.now())
                );
                AlertUtils.showSuccess("Commande validee avec paiement a la livraison. ID commande : " + commandeId);
                SceneNavigator.switchScene(paysField, "/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur lors de la validation de la livraison : " + e.getMessage());
            }
            return;
        }

        AlertUtils.showSuccess("Coordonnees de livraison enregistrees.");
        SceneNavigator.switchScene(paysField, "/payment-view.fxml", "Paiement");
    }

    private void loadSummary() {
        subtotalLabel.setText(String.format("%.2f TND", OrderSession.getInstance().getCartTotal()));
        totalSummaryLabel.setText(String.format("%.2f TND", OrderSession.getInstance().getCartTotal()));

        Commande draft = OrderSession.getInstance().getDraftCommande();
        if (draft != null && draft.getPaysLivraison() != null && !draft.getPaysLivraison().isBlank()) {
            loadSavedDeliveryAddress(draft);
        } else if (draft != null && draft.getAdresse() != null && draft.getAdresse().contains("Livraison")) {
            loadLegacyDeliveryAddress(draft.getAdresse());
        }
    }

    private Commande readForm() {
        if (OrderSession.getInstance().isCartEmpty()) {
            AlertUtils.showError("Le panier est vide.");
            return null;
        }

        String pays = paysField.getText().trim();
        String gouvernorat = gouvernoratField.getText().trim();
        String codePostal = codePostalField.getText().trim();
        String adresse = adresseField.getText().trim();
        String adresseDetail = adresseDetailArea.getText().trim();

        if (pays.isEmpty() || gouvernorat.isEmpty() || codePostal.isEmpty() || adresse.isEmpty() || adresseDetail.isEmpty()) {
            AlertUtils.showError("Remplissez toutes les coordonnees de livraison.");
            return null;
        }

        Commande draft = OrderSession.getInstance().getDraftCommande();
        Commande commande = draft == null
                ? new Commande(java.sql.Date.valueOf(java.time.LocalDate.now()), OrderSession.getInstance().getCartTotal(), resolveCurrentUserId())
                : draft;

        commande.setDateCommande(java.sql.Date.valueOf(java.time.LocalDate.now()));
        commande.setTotal(OrderSession.getInstance().getCartTotal());
        commande.setPaysLivraison(pays);
        commande.setGouvernoratLivraison(gouvernorat);
        commande.setCodePostalLivraison(codePostal);
        commande.setAdresseLivraison(adresse);
        commande.setDescriptionLivraison(adresseDetail);
        commande.setStatut(OrderSession.getInstance().getCheckoutMode() == OrderSession.CheckoutMode.CASH_ON_DELIVERY
                ? "EN_LIVRAISON"
                : "EN_ATTENTE_PAIEMENT");
        return commande;
    }

    private int resolveCurrentUserId() {
        User currentUser = DashboardSession.getCurrentUser();
        return currentUser == null ? AUTO_CLIENT_ID_PLACEHOLDER : currentUser.getId();
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private void loadSavedDeliveryAddress(Commande draft) {
        paysField.setText(emptyIfNull(draft.getPaysLivraison()));
        gouvernoratField.setText(emptyIfNull(draft.getGouvernoratLivraison()));
        codePostalField.setText(emptyIfNull(draft.getCodePostalLivraison()));
        adresseField.setText(emptyIfNull(draft.getAdresseLivraison()));
        adresseDetailArea.setText(emptyIfNull(draft.getDescriptionLivraison()));
    }

    private void loadLegacyDeliveryAddress(String source) {
        paysField.setText(emptyIfNull(extractLegacyDeliveryValue(source, "Pays:")));
        gouvernoratField.setText(emptyIfNull(extractLegacyDeliveryValue(source, "Gouvernorat:")));
        codePostalField.setText(emptyIfNull(extractLegacyDeliveryValue(source, "Code postal:")));
        adresseField.setText(emptyIfNull(extractLegacyDeliveryValue(source, "Adresse:")));
        adresseDetailArea.setText(emptyIfNull(extractLegacyDeliveryValue(source, "Description:")));
    }

    private String extractLegacyDeliveryValue(String source, String label) {
        String[] lines = source.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.startsWith(label)) {
                continue;
            }

            String value = line.substring(label.length()).trim();
            if (!value.isBlank()) {
                return value;
            }

            if (i + 1 < lines.length) {
                return lines[i + 1].trim();
            }
        }
        return "";
    }

    private void applySelectedPlace(SelectedPlace place) {
        selectedPlace = place;
        paysField.setText(place.country());
        gouvernoratField.setText(place.governorate());
        codePostalField.setText(place.postcode());
        adresseField.setText(place.address());
        adresseDetailArea.setText(place.description());
    }

    private String buildMapHtml() {
        double startLat = 34.0;
        double startLon = 9.0;
        int zoom = 5;

        return """
                <!doctype html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                    <style>
                        html, body {
                            height: 100%;
                            width: 100%;
                            margin: 0;
                            padding: 0;
                            overflow: hidden;
                            background: #11182f;
                        }
                        #map {
                            position: absolute;
                            inset: 0;
                            height: 100vh;
                            width: 100vw;
                            min-height: 100vh;
                            min-width: 100vw;
                            background: #11182f;
                        }
                        .leaflet-control-attribution { font: 11px sans-serif; }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        const map = L.map('map').setView([__START_LAT__, __START_LON__], __ZOOM__);
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '&copy; OpenStreetMap'
                        }).addTo(map);

                        function refreshMapSize() {
                            map.invalidateSize(true);
                        }

                        window.addEventListener('load', refreshMapSize);
                        window.addEventListener('resize', refreshMapSize);
                        setTimeout(refreshMapSize, 100);
                        setTimeout(refreshMapSize, 400);
                        setTimeout(refreshMapSize, 900);

                        let marker;
                        map.on('click', function (event) {
                            const lat = event.latlng.lat;
                            const lng = event.latlng.lng;
                            if (!marker) {
                                marker = L.marker([lat, lng], { draggable: true }).addTo(map);
                                marker.on('dragend', function () {
                                    const pos = marker.getLatLng();
                                    window.javaBridge.markerChanged(pos.lat, pos.lng);
                                });
                            } else {
                                marker.setLatLng([lat, lng]);
                            }
                            window.javaBridge.markerChanged(lat, lng);
                        });
                    </script>
                </body>
                </html>
                """
                .replace("__START_LAT__", String.format(Locale.US, "%.6f", startLat))
                .replace("__START_LON__", String.format(Locale.US, "%.6f", startLon))
                .replace("__ZOOM__", String.valueOf(zoom));
    }

    public class MapBridge {
        private final Label statusLabel;
        private final Button confirmButton;
        private SelectedPlace place;

        MapBridge(Label statusLabel, Button confirmButton) {
            this.statusLabel = statusLabel;
            this.confirmButton = confirmButton;
        }

        public void markerChanged(double latitude, double longitude) {
            Platform.runLater(() -> {
                statusLabel.setText(String.format(Locale.US, "Recherche adresse %.5f, %.5f ...", latitude, longitude));
                confirmButton.setDisable(true);
            });

            reverseGeocode(latitude, longitude);
        }

        private void reverseGeocode(double latitude, double longitude) {
            String url = String.format(Locale.US, NOMINATIM_REVERSE_URL, latitude, longitude);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .header("User-Agent", "PROJETPI-DeliveryMap/1.0")
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> parsePlace(body, latitude, longitude))
                    .exceptionally(error -> SelectedPlace.fromCoordinates(latitude, longitude))
                    .thenAccept(foundPlace -> Platform.runLater(() -> {
                        place = foundPlace;
                        if (foundPlace.hasDeliveryAddress()) {
                            statusLabel.setText("Position selectionnee : " + foundPlace.displayLabel());
                            confirmButton.setDisable(false);
                        } else {
                            statusLabel.setText("Adresse introuvable pour cette position. Choisis un point sur une zone habitee.");
                            confirmButton.setDisable(true);
                        }
                    }));
        }
    }

    private SelectedPlace parsePlace(String json, double latitude, double longitude) {
        String country = jsonValue(json, "country");
        String governorate = firstNotBlank(
                jsonValue(json, "state"),
                jsonValue(json, "governorate"),
                jsonValue(json, "region"),
                jsonValue(json, "county")
        );
        String postcode = jsonValue(json, "postcode");
        String city = firstNotBlank(jsonValue(json, "city"), jsonValue(json, "town"), jsonValue(json, "village"));
        String road = firstNotBlank(jsonValue(json, "road"), jsonValue(json, "neighbourhood"), jsonValue(json, "suburb"));
        String address = firstNotBlank(joinNonBlank(", ", road, city), jsonValue(json, "display_name"));
        String description = String.format(Locale.US, "Position GPS: %.6f, %.6f", latitude, longitude);

        if (country.isBlank() || address.isBlank()) {
            return SelectedPlace.fromCoordinates(latitude, longitude);
        }

        return new SelectedPlace(country, governorate, postcode, address, description);
    }

    private String jsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? unescapeJson(matcher.group(1)) : "";
    }

    private String unescapeJson(String value) {
        String basicValue = value
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .replace("\\n", " ")
                .replace("\\r", " ")
                .replace("\\t", " ");

        Matcher unicodeMatcher = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(basicValue);
        StringBuilder decoded = new StringBuilder();
        while (unicodeMatcher.find()) {
            String character = String.valueOf((char) Integer.parseInt(unicodeMatcher.group(1), 16));
            unicodeMatcher.appendReplacement(decoded, Matcher.quoteReplacement(character));
        }
        unicodeMatcher.appendTail(decoded);
        return decoded.toString();
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String joinNonBlank(String delimiter, String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private record SelectedPlace(
            String country,
            String governorate,
            String postcode,
            String address,
            String description
    ) {
        private static SelectedPlace fromCoordinates(double latitude, double longitude) {
            String coordinates = String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
            return new SelectedPlace("", "", "", "", "Position GPS: " + coordinates);
        }

        private boolean hasDeliveryAddress() {
            return country != null && !country.isBlank()
                    && address != null && !address.isBlank();
        }

        private String displayLabel() {
            if (country != null && !country.isBlank()) {
                return country;
            }
            if (address != null && !address.isBlank()) {
                return address;
            }
            return description == null ? "" : description;
        }
    }
}
