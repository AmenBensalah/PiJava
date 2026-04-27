# README 2 - Integrations RAWG.io, CalendarFX et Prediction IA

## 1) Objectif
Ce document explique comment travailler les 3 integrations principales du projet:
- API jeux `RAWG.io`
- calendrier `CalendarFX`
- bloc `Prediction IA` (admin)

Il est pense comme guide pratique: **ou est le code**, **quoi modifier**, **dans quel ordre tester**.

---

## 2) Emplacement rapide des codes

### RAWG.io
- Service API: `src/main/java/edu/connexion3a77/services/RawgApiService.java`
- Controller UI: `src/main/java/edu/connexion3a77/controllers/RawgGamesController.java`
- Entite de resultat: `src/main/java/edu/connexion3a77/entities/RawgGame.java`
- Vue FXML: `src/main/resources/fxml/rawg-games-view.fxml`
- CSS: `src/main/resources/css/rawg-games.css`
- Navigation vers ecran RAWG: `src/main/java/edu/connexion3a77/ui/SceneNavigator.java`

### CalendarFX
- Controller user (initialisation + remplissage): `src/main/java/edu/connexion3a77/controllers/TournoiUserController.java`
- Zone d'accueil calendrier (FXML): `src/main/resources/fxml/tournoi-user-view.fxml` (`fx:id="calendarContainer"`)
- Dependance Maven: `pom.xml` (`com.calendarfx:view`)

### Prediction IA
- Script Python: `src/main/resources/predict_tournoi.py`
- Dataset CSV: `src/main/resources/tournois_stats.csv`
- Bloc UI admin (cartes prediction): `src/main/resources/fxml/tournoi-admin-view.fxml`
- Style prediction: `src/main/resources/css/tournoi-admin.css`
- Logique prediction admin: `src/main/java/edu/connexion3a77/controllers/TournoiAdminController.java`

---

## 3) RAWG.io - Comment ca marche

## 3.1 Flux technique
1. L'utilisateur tape un nom de jeu.
2. `RawgGamesController` appelle `RawgApiService.searchGames(query, pageSize)`.
3. `RawgApiService` construit l'URL RAWG:
   - base: `https://api.rawg.io/api/games`
   - params: `key`, `page_size`, `search`
4. JSON recu -> parsing vers objets `RawgGame`.
5. Le controller affiche des cards dans `gamesCardsPane`.

## 3.2 Configuration cle API
Le service cherche la cle dans cet ordre:
1. variable d'environnement `RAWG_API_KEY`
2. propriete JVM `rawg.api.key`
3. fichier `config/local.properties` (`rawg.api.key=...`)

Conseil:
- garde ta vraie cle dans `config/local.properties` (deja supporte),
- ne publie pas la cle dans les captures/README publics.

## 3.3 Ce que tu modifies souvent
- Changer nb resultats: `searchGames(query, 18)` dans `RawgGamesController`
- Changer timeout HTTP: `RawgApiService` (`connectTimeout`, `request timeout`)
- Ajouter champs UI: editer `buildGameCard(...)` dans `RawgGamesController`

---

## 4) CalendarFX - Comment ca marche

## 4.1 Initialisation
Dans `TournoiUserController`:
- `initCalendarView()` cree:
  - un `Calendar`
  - un `CalendarSource`
  - un `CalendarView`
- puis injecte la vue dans `calendarContainer`.

## 4.2 Alimentation des evenements
Toujours dans `TournoiUserController`:
- `refreshCalendarFromParticipations()`:
  - vide le calendrier
  - lit les participations
  - garde seulement statut `ACCEPTEE`
  - cree des `Entry` (nom tournoi, date debut/fin, lieu = jeu)

## 4.3 Quand il se rafraichit
Le calendrier est rafraichi apres:
- chargement initial
- ajout/modif/suppression participation
- action `Rejoindre`
- bouton refresh user

## 4.4 Personnalisation rapide
- Style de calendrier: `userTournamentsCalendar.setStyle(...)`
- Vue par defaut: `calendarView.showMonthPage()` (tu peux passer en semaine/jour)
- Heure de focus: `setRequestedTime(...)`

---

## 5) Prediction IA - Comment ca marche

## 5.1 Partie Python
Script: `src/main/resources/predict_tournoi.py`
- charge `tournois_stats.csv`
- entraine deux `RandomForestClassifier`
- predit:
  - `type_tournoi`
  - `jeu`

Run manuel:
```powershell
.\python\venv\Scripts\python.exe .\src\main\resources\predict_tournoi.py
```

## 5.2 Partie admin JavaFX
Dans l'admin:
- le panneau prediction est dans `tournoi-admin-view.fxml`
- styles dans `tournoi-admin.css`
- logique dans `TournoiAdminController`:
  - `onGeneratePrediction()`
  - `refreshPredictionPanel()`
  - `buildPrediction()`

Note actuelle:
- le panneau admin calcule une prediction basee sur les tendances en base Java.
- le script Python est pret en parallele pour une integration ML reelle.

## 5.3 Pour brancher Java -> Python (etape suivante)
Option recommandee:
1. Dans `TournoiAdminController`, creer une methode qui lance:
   - `python.exe predict_tournoi.py` via `ProcessBuilder`
2. Lire la sortie console Python.
3. Parser les lignes `Type...` et `Jeu...`.
4. Injecter dans:
   - `lblPredictionType`
   - `lblPredictionJeu`
   - `predictionStatusLabel`

---

## 6) Workflow conseille quand tu developpes
1. Verifier DB + UI Java (tournois/participations).
2. Verifier RAWG avec une recherche simple (`valorant`, `fifa`).
3. Verifier CalendarFX (une participation acceptee doit apparaitre).
4. Verifier script Python depuis terminal.
5. Verifier bloc prediction admin.

---

## 7) Checklist debug rapide

### RAWG ne marche pas
- verifier internet
- verifier cle API
- verifier `config/local.properties`
- verifier message dans `gamesStatusLabel`

### Calendar vide
- verifier qu'il existe des participations `ACCEPTEE`
- verifier mapping `tournoiId -> Tournoi`
- verifier `dateDebut` et `dateFin`

### Prediction Python en erreur
- lancer avec le python du venv
- verifier packages (`pandas`, `scikit-learn`)
- verifier presence `tournois_stats.csv`

---

## 8) Commandes utiles
Lancer app JavaFX (Maven wrapper):
```powershell
.\mvnw.cmd javafx:run
```

Lancer tests:
```powershell
.\mvnw.cmd test
```

Lancer prediction Python:
```powershell
.\python\venv\Scripts\python.exe .\src\main\resources\predict_tournoi.py
```

---

## 9) Resume en 3 lignes
- `RAWG`: service HTTP + affichage cards jeux.
- `CalendarFX`: calendrier user alimente par participations acceptees.
- `Prediction IA`: script Python pret + panneau admin deja integre.
