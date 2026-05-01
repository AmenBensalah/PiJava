# Explication détaillée de la Prédiction de Victoire (Win Prediction)

Ce document explique étape par étape comment fonctionne le système de prédiction de victoire (Win Rate/Performance) pour chaque utilisateur. Tout comme pour le Face ID, ce guide te fournira les arguments techniques précis pour ta validation.

## 1. Les Fichiers Clés de la Fonctionnalité

La prédiction est gérée par un agent IA écrit en **Python** (dans le dossier `user_ai_agent/src/`), qui communique ensuite avec ton application Java.

1. **`feature_engineering.py` (Préparation des Données)**
   * **Rôle :** Transforme l'historique brut des matchs des utilisateurs en données mathématiques exploitables par une Intelligence Artificielle (création de *features*).
2. **`train_user_performance.py` (Le Cerveau / Machine Learning)**
   * **Rôle :** Entraîne des algorithmes d'IA sur l'historique pour apprendre les modèles de victoire/défaite, évalue ces modèles, et calcule la probabilité de victoire d'un utilisateur pour son prochain match dans différents types de jeux (FPS, Sports, Battle Royale, etc.).

---

## 2. Le Processus Étape par Étape

Voici comment l'IA prédit les performances d'un utilisateur :

### A. La Création des Caractéristiques (Feature Engineering)
L'IA ne peut pas deviner si tu vas gagner juste avec ton nom. Le script `feature_engineering.py` va lire tout ton historique de matchs et calculer des statistiques temporelles à l'instant T (avant ton prochain match). 
Les principales "features" calculées sont :
* **Historique Global :** Ton taux de victoire général (`prev_win_rate`), ta moyenne de points (`prev_avg_points`).
* **La Forme du moment :** Ton score sur tes 5 ou 10 derniers matchs (`prev_form5_score`, `recent_win_streak`).
* **Historique par Type de Jeu :** Ton taux de victoire spécifique sur le jeu auquel tu vas jouer (ex: FPS ou Sports).
* **Le contexte :** Est-ce un match en équipe ou en solo ? (`is_squad`).

### B. L'Entraînement Dynamique (Auto-ML basique)
Au lieu de coder un algorithme en dur, le système est intelligent et s'adapte. Dans `train_user_performance.py`, le système utilise la bibliothèque **scikit-learn** pour tester **3 algorithmes de Machine Learning différents** :
1. **Logistic Regression** (Régression Logistique)
2. **Random Forest Classifier** (Forêt Aléatoire)
3. **Hist Gradient Boosting** 

Le script utilise une technique appelée **Validation Croisée (Cross-Validation / StratifiedKFold)**. Il entraîne et teste ces trois modèles sur tes données, compare leur score de précision (*Balanced Accuracy*), et **garde automatiquement le meilleur des trois** pour faire la prédiction finale.

### C. La Prédiction et le Lissage (Blending / Cold Start)
Un grand problème en IA est le "Cold Start" : comment prédire si un joueur va gagner s'il n'a fait que 1 ou 2 matchs ?
Pour éviter que l'IA ne dise n'importe quoi, le code utilise une fonction de lissage mathématique (`_blend_probability`). La probabilité finale de victoire est un mélange intelligent (une moyenne pondérée) entre :
1. **La prédiction pure de l'IA** (qui a beaucoup de poids si le joueur a beaucoup joué).
2. **Le taux de victoire historique du joueur** sur ce jeu.
3. **La moyenne globale (baseline) de tous les joueurs** sur ce jeu (très utile si le joueur est un débutant total sur ce jeu).

### D. Export et Affichage
Le système génère des probabilités pour tous les types de jeux (FPS, Sports, Mind, etc.). 
* Il détermine quel est le **Meilleur Type de Jeu** pour cet utilisateur (`bestGameType`).
* Le tout est exporté dans un fichier `predictions.json`.
* Ton application Java lit ce fichier JSON et affiche proprement le pourcentage de chance de victoire (ex: 65.4%) et la recommandation de jeu sur l'interface (Profil Utilisateur).

---

## 3. Le Dataset Utilisé (Origine des données)

Si le professeur te demande **"D'où vient ton dataset ? L'as-tu téléchargé sur Kaggle ?"**, la réponse est **Non**. 

Ton dataset est **100% dynamique et réel**, il provient directement de la base de données MySQL de ton application. 
Voici comment il est construit :
1. **Extraction (`extract_events.py`) :** Un script se connecte à la base de données de l'application via `SQLAlchemy`.
2. **Croisement des tables :** Il joint les données des tables `users`, `tournois`, `matches`, `teams`, et `match_participants`.
3. **Génération :** Il reconstitue l'historique complet pour savoir qui a joué contre qui, dans quel jeu, et le résultat final (Victoire, Défaite, Nul). Il exporte ensuite ces données brutes dans un fichier local appelé `raw_events.csv`.
4. **Apprentissage sur données réelles :** L'IA s'entraîne sur ce fichier généré localement. Il n'y a donc pas de dataset statique externe. Ton IA apprend sur les **vraies données générées par les utilisateurs de ton application**.

---

## 💡 Résumé pour ta validation (Ce que le prof veut entendre)
* "Nous utilisons du **Machine Learning supervisé** avec la bibliothèque Python **scikit-learn**."
* "Le système est robuste : il ne repose pas sur un seul algorithme. Il teste dynamiquement la Régression Logistique, Random Forest et Gradient Boosting via **Validation Croisée**, et sélectionne automatiquement le plus performant pour notre jeu de données."
* "Nous faisons du **Feature Engineering** pour extraire la dynamique du joueur (ses séries de victoires, sa forme sur les 5 derniers matchs, ses statistiques spécifiques par type de jeu)."
* "Nous gérons le problème des nouveaux joueurs (Cold Start) en appliquant un **Blending (lissage)** entre la prédiction du modèle IA et la moyenne statistique globale du jeu."
