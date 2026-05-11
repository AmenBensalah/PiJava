<p align="center">
  <img src="./src/main/resources/images/banner.png" alt="Bannière E-sportify" width="900">
</p>

# 🎮 Esportify - Plateforme de Gestion E-Sport (JavaFX)

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java Version">
  <img src="https://img.shields.io/badge/JavaFX-17-0A84FF?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX Version">
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Stripe-6772E5?style=for-the-badge&logo=stripe&logoColor=white" alt="Stripe">
  <img src="https://img.shields.io/badge/AI_Powered-FF6F61?style=for-the-badge" alt="AI Powered">
  <img src="https://img.shields.io/badge/License-Academic-red?style=for-the-badge" alt="License">
</p>

---

## 🌟 Overview

Ce projet a été développé dans le cadre du programme PIDEV – 3ème année du cycle ingénieur à **École Supérieure Privée d’Ingénierie et de Technologies (ESPRIT)** (Année Universitaire 2025–2026).

**Esportify** est une plateforme desktop complète et moderne conçue pour l’industrie de l’e-sport compétitif. Elle relie joueurs, équipes et organisateurs de tournois via un écosystème unifié : gestion des compétitions, recrutement, engagement communautaire, boutique e-commerce, et paiements sécurisés, avec une identité visuelle cyberpunk.

---

## 🚀 Features

- **🏆 Gestion des Tournois** : Gestion du cycle de vie des tournois (création, modification, suivi), participation des utilisateurs, modération admin, intégration des jeux via **RAWG API**.
- **👥 Écosystème Joueurs & Équipes** : Création/gestion d’équipes, candidatures, demandes manager, workflows de recrutement structurés.
- **🛒 Boutique E-Sport** : Gestion des produits, catégories, commandes, livraison, et parcours d’achat avec paiements **Stripe**.
- **💬 Hub de Communication** : Fil d’actualité, publications, commentaires, annonces, messagerie et notifications par e-mail via **Brevo API**.
- **🤖 Intelligence Artificielle et Sécurité** : Services IA (recommandation, prédiction, assistance métier), OAuth Google/Discord, modules de sécurité et validation.
- **📊 Tableaux de Bord & Outils** : Interfaces admin/user, reporting opérationnel, génération de factures et rapports au format PDF.

---

## 🛠️ Tech Stack

### 🎨 Frontend Desktop
- **JavaFX 17** (FXML + CSS)
- Architecture UI par vues modulaires (`views/`, `fxml/`)
- Navigation multi-écrans (admin/user/manager)

### ⚙️ Backend Applicatif
- **Java 17**
- **Maven 3.9+**
- **MySQL** & **JDBC**
- Architecture orientée services (controllers/services/entities)

### 🔗 APIs Intégrées / Services
- **Stripe API** : Paiement sécurisé et confirmation des transactions.
- **Brevo API** : Envoi d’e-mails système et transactionnels.
- **OAuth2 Google / Discord** : Connexion sociale.
- **RAWG API** : Données de jeux e-sport.
- **Gemini / modules IA** : Assistants, prédictions et recommandations.

---

## 🏗️ Architecture

Le projet suit une architecture en couches inspirée du modèle MVC desktop :

```text
src/main/java/
├── edu/esportify/       # Core app (controllers, services, entities, navigation)
├── edu/connexion3a77/   # Module tournois (tournoi, participation, RAWG)
├── edu/PROJETPI/        # Module comptes, paiements, checkout, livraison
└── edu/projetJava/      # Module boutique/backoffice et IA produit

src/main/resources/
├── views/               # Vues FXML principales
├── fxml/                # Vues FXML spécialisées
├── styles/ + css/       # Thèmes et styles UI
├── images/              # Assets visuels (logo, bannière, backgrounds)
├── db.properties        # Configuration base de données
└── stripe.properties    # Configuration Stripe
```

Flux principal :
`FXML (Vue) -> Controller -> Service -> JDBC/MySQL -> Retour Controller -> Mise à jour UI`

---

## 👥 Contributors

Groupe **Esportify** :
- Amen Bensalah
- Mohamed Bouzid
- Ilyes Zid
- Aysser Dhifallah
- Mohamed Ghaieth Bouamor
- Youssef Mejri

---

## 🎓 Academic Context

Developed at **École Supérieure Privée d’Ingénierie et de Technologies (ESPRIT) – Tunisia**  
PIDEV – 3A | 2025 – 2026

---

## 📦 Getting Started

### ✅ Prérequis
- JDK 17
- Maven 3.9+ (ou `mvnw` / `mvnw.cmd`)
- MySQL 8.0+

### 💽 Étapes d'installation

1. **Cloner le dépôt**
   ```bash
   git clone <votre-url-github>
   cd Connexion3A377
   ```

2. **Configurer l’environnement**
   Copier `.env.example` vers `.env` et renseigner vos variables locales :
   ```bash
   cp .env.example .env
   ```

3. **Configurer la base de données**
   Vérifier `src/main/resources/db.properties` puis importer le dump officiel :

   PowerShell :
   ```powershell
   ./scripts/import-esportify-db.ps1
   ```

   Git Bash / Linux :
   ```bash
   bash ./scripts/import-esportify-db.sh
   ```

4. **Compiler le projet**
   ```bash
   mvn clean compile
   ```

5. **Lancer l’application JavaFX**
   ```bash
   mvn javafx:run
   ```

   *L’application s’ouvre dans une fenêtre desktop locale.*

6. **Exécuter les tests**
   ```bash
   mvn test
   ```

---

## 🙏 Acknowledgments

Nous tenons à exprimer nos remerciements à notre encadrante, Madame **Ayari Asma**, pour son accompagnement, ses conseils et son soutien tout au long du développement de ce projet.

Un grand merci également à **ESPRIT** pour l’environnement pédagogique, les ressources techniques et l’encadrement académique de qualité.

