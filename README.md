<p align="center">
  <img src="./src/main/resources/images/banner.png" alt="Bannière E-sportify" width="900">
</p>

# E-sportify

<p align="center">
  Plateforme desktop de gestion e-sport, construite avec JavaFX, Maven et MySQL.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/JavaFX-17-0A84FF?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX 17">
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
</p>

---

## Aperçu

E-sportify est une application desktop modulaire qui centralise les principaux workflows e-sport dans une seule plateforme:
- gestion des tournois,
- gestion des équipes et du recrutement,
- fil d'actualité communautaire et modération,
- boutique, commandes et paiements,
- services complémentaires assistés par IA.

Le projet suit une architecture Java en couches: interface JavaFX, logique métier orientée services, et persistance MySQL via JDBC.

---

## Fonctionnalités principales

- Gestion complète du cycle de vie des tournois (vues admin et user)
- Gestion des demandes de participation et de la modération
- Gestion des équipes, candidatures et demandes manager
- Fil social avec posts, commentaires et annonces
- Gestion de boutique (produits et catégories)
- Panier, checkout, commandes, livraison et paiements
- Génération de factures et rapports PDF
- Authentification avec options OAuth (Google / Discord)
- Intégration e-mail et notifications
- Modules assistés par IA (recommandation, prédiction, services intelligents)

---

## Stack technique

### Application
- Java 17
- JavaFX 17 (FXML + CSS)
- Maven
- MySQL + JDBC

### Bibliothèques clés
- `mysql-connector-j`
- `stripe-java`
- `gson`
- `pdfbox`, `itextpdf`
- `javax.mail`
- `onnxruntime`, `webcam-capture`

### Tests
- JUnit 5
- Mockito
- H2 (scope test)

---

## Structure du projet

```text
src/main/java/
|-- edu/esportify/      # Modules principaux (controllers/services/entities/navigation)
|-- edu/connexion3a77/  # Modules orientés tournois
|-- edu/PROJETPI/       # Modules paiements/comptes/support
'-- edu/projetJava/     # Modules boutique/backoffice

src/main/resources/
|-- views/              # Vues JavaFX principales
|-- fxml/               # Vues JavaFX additionnelles
|-- styles/, css/       # Thèmes et styles UI
|-- images/             # Assets visuels
|-- db.properties       # Configuration DB par défaut
'-- stripe.properties   # Configuration Stripe
```

---

## Démarrage rapide

### Prérequis
- JDK 17
- Maven 3.9+ (ou wrappers `mvnw` / `mvnw.cmd`)
- MySQL 8+

### 1) Cloner le projet
```bash
git clone <url-du-repository>
cd Connexion3A377
```

### 2) Configurer l'environnement
Copier puis adapter les variables:
```bash
cp .env.example .env
```

Renseigner les valeurs nécessaires (base de données, Stripe, OAuth, Brevo, Gemini, RAWG, SMTP).

### 3) Configurer la base de données
Vérifier:
- `src/main/resources/db.properties`

Puis importer le schéma fourni:

PowerShell:
```powershell
./scripts/import-esportify-db.ps1
```

Git Bash / Linux:
```bash
bash ./scripts/import-esportify-db.sh
```

Emplacement du dump SQL:
- `database/esportify-2.sql`

### 4) Compiler et lancer
```bash
mvn clean compile
mvn javafx:run
```

Alternative Windows:
```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd javafx:run
```

### 5) Lancer les tests
```bash
mvn test
```

---

## Notes de configuration

- Conserver `.env` en local et ne jamais versionner de secrets.
- Les fonctionnalités externes qui demandent des clés API sont optionnelles; les flux de base peuvent fonctionner sans toutes les intégrations.
- Pour le développement local, garder la configuration DB cohérente entre `.env` et `db.properties`.

---

## Documentation complémentaire

Des notes techniques additionnelles sont disponibles dans:
- `README_2_INTEGRATIONS.md`
- `face_id_explanation.md`
- `win_prediction_explanation.md`

---

## Licence

Ce dépôt est actuellement distribué dans un cadre académique/projet. Ajoutez une licence explicite dans `LICENSE` si vous prévoyez une réutilisation publique.
