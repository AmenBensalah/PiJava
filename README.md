# Connexion3A377 - Gestion de Tournois (JavaFX + JDBC + Maven)

## 1) Presentation generale
Ce projet est une application Java de gestion de tournois e-sport avec:
- une interface graphique JavaFX (admin + user),
- un acces base de donnees MySQL via JDBC,
- une gestion du build/test avec Maven.

L'application permet de:
- creer/modifier/supprimer des tournois,
- consulter les tournois cote user,
- envoyer des demandes de participation,
- accepter/refuser les demandes cote admin.

---

## 2) Architecture globale du code
Le projet suit une separation en couches simple et claire:

- `entities/`:
  - classes metier (modeles): `Tournoi`, `DemandeParticipation`.
  - elles representent les donnees manipulees.

- `interfaces/`:
  - contrat generique `IService<T>` pour les operations CRUD.

- `services/`:
  - logique metier + acces JDBC.
  - `TournoiService` et `DemandeParticipationService`.
  - creation des tables si elles n'existent pas.

- `tools/`:
  - outils techniques partages.
  - `MyConnection` gere la connexion MySQL (pattern singleton).

- `controllers/`:
  - logique de l'interface JavaFX (actions boutons, validation formulaire, rafraichissement tables/cartes).
  - relie la vue (`fxml`) aux services.

- `ui/`:
  - demarrage application et navigation des scenes.
  - `TournoiAdminApp`: point d'entree JavaFX.
  - `SceneNavigator`: changement de vue admin/user.

- `resources/fxml`:
  - structure visuelle des ecrans (declaratif XML).

- `resources/css`:
  - style graphique des vues.

- `test/`:
  - tests JUnit des services.

Flux principal:
`FXML (vue) -> Controller -> Service -> JDBC/MySQL -> retour Controller -> mise a jour UI`

---

## 3) C'est quoi Maven ?
Maven est un outil de gestion de projet Java:
- il telecharge les dependances (JavaFX, MySQL Connector, JUnit),
- il compile le code,
- il execute les tests,
- il lance l'application (via plugin JavaFX),
- il standardise la structure du projet.

Le fichier central est `pom.xml`:
- dependances du projet,
- version Java (`17` ici),
- plugins (Surefire pour tests, JavaFX Maven plugin pour execution UI).

Commandes utiles:
- compiler: `mvn clean compile`
- lancer les tests: `mvn test`
- lancer JavaFX: `mvn javafx:run`

---

## 4) Statement vs PreparedStatement
### `Statement`
- Requete SQL envoyee telle quelle (souvent construite en concatenant du texte).
- Moins sur pour les donnees utilisateur (risque d'injection SQL).
- Utile surtout pour des requetes fixes sans parametres (ex: DDL simple).

Exemple dans ce projet:
- creation de table (`CREATE TABLE IF NOT EXISTS`) dans les services.

### `PreparedStatement`
- Requete precompilee avec parametres `?`.
- Plus securisee (protection contre injection SQL).
- Plus propre pour inserer/modifier/rechercher avec valeurs dynamiques.
- Meilleure pratique pour presque toutes les operations CRUD.

Exemple dans ce projet:
- `INSERT`, `UPDATE`, `DELETE`, `SELECT` avec parametres (id, nom, dates, etc.).

### Quand utiliser quoi ?
- Utilise `PreparedStatement` par defaut.
- Garde `Statement` seulement pour des requetes SQL fixes sans saisie utilisateur.

---

## 5) executeUpdate vs executeQuery
### `executeUpdate()`
Utilisee pour les requetes qui modifient la base:
- `INSERT`
- `UPDATE`
- `DELETE`
- DDL (`CREATE TABLE`, `ALTER TABLE`, etc.)

Retour: un `int` = nombre de lignes impactees (ou 0 pour certaines requetes DDL).

### `executeQuery()`
Utilisee pour les requetes de lecture:
- `SELECT`

Retour: un `ResultSet` a parcourir ligne par ligne.

### Regle simple
- si la requete "lit" des donnees -> `executeQuery()`
- si la requete "change" la base -> `executeUpdate()`

---

## 6) C'est quoi le package/fichier `ui` ?
Dans ce projet, `ui` contient la partie "navigation et lancement":

- `TournoiAdminApp.java`
  - classe qui herite de `Application` JavaFX,
  - methode `main()` + `start(Stage)` pour ouvrir la fenetre.

- `SceneNavigator.java`
  - centralise le changement de scene (admin/user),
  - charge les fichiers FXML et CSS,
  - fixe le titre et affiche la fenetre.

Important:
- `ui` ne contient pas la logique metier SQL.
- la logique metier est dans `services`.
- la logique interaction utilisateur est dans `controllers`.

---

## 7) Schema de base de donnees (resume)
Tables principales:

1. `tournoi`
- `id` (PK auto increment)
- `nom_tournoi`
- `type_tournoi`
- `nom_jeu`
- `date_debut`
- `date_fin`
- `nombre_participants`
- `cash_prize`

2. `demande_participation`
- `id` (PK auto increment)
- `tournoi_id`
- `description`
- `niveau`

---

## 8) Configuration et execution
### Prerequis
- JDK 17
- MySQL en local
- Maven (ou `mvnw`/`mvnw.cmd`)

### Connexion DB actuelle
Dans `MyConnection.java`:
- URL: `jdbc:mysql://localhost:3306/javafx`
- user: `root`
- password: `""` (vide)

Adapte ces valeurs selon ton environnement.

### Lancer le projet
- Windows: `mvnw.cmd javafx:run`
- Linux/macOS: `./mvnw javafx:run`

### Lancer les tests
- Windows: `mvnw.cmd test`
- Linux/macOS: `./mvnw test`

---

## 9) Reponses rapides pour validation (questions prof)
1. **Pourquoi Maven ?**  
Pour automatiser compilation/tests/execution et gerer les dependances depuis `pom.xml`.

2. **Architecture du projet ?**  
Architecture en couches: `UI/FXML -> Controllers -> Services -> MySQL`, avec `entities` comme modeles.

3. **Pourquoi PreparedStatement est prefere ?**  
Securite (anti injection SQL), lisibilite, parametres types, et bonnes pratiques JDBC.

4. **Quand utiliser Statement ?**  
Principalement pour des requetes fixes sans parametres (ex: creation de table).

5. **Difference executeQuery / executeUpdate ?**  
`executeQuery` pour `SELECT`; `executeUpdate` pour `INSERT/UPDATE/DELETE/DDL`.

6. **Role du package ui ?**  
Lancement JavaFX + navigation entre ecrans, sans logique metier SQL.

7. **Ou est la logique metier ?**  
Dans `TournoiService` et `DemandeParticipationService` (validation + CRUD).

---

## 10) Points techniques a citer a l'oral
- Utilisation de JavaFX (`FXML + Controller + CSS`) pour separer presentation et logique.
- Utilisation de `IService<T>` pour uniformiser les operations CRUD.
- Utilisation d'un singleton (`MyConnection`) pour partager la connexion DB.
- Presence de tests unitaires JUnit pour verifier CRUD des services.
