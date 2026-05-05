# Matching IA Training

Ce dossier contient un exemple concret d'entrainement Python pour le `Matching IA`.

## Objectif

Predire si une equipe est un bon match pour un joueur en fonction de:

- role du joueur
- region
- niveau
- disponibilite
- objectif de performance
- region et niveau de l'equipe
- activite de l'equipe
- nombre de recrutements ouverts
- role recherche par l'equipe

## Fichiers

- `train_matching_model.py`: script d'entrainement
- `data/matching_training_dataset.csv`: dataset d'exemple
- `output/`: modele entraine et metadata

## Installation

```bash
pip install pandas scikit-learn joblib
```

## Lancer l'entrainement

```bash
python scripts/matching_ia/train_matching_model.py
```

## Sorties

Le script genere:

- `scripts/matching_ia/output/matching_model.joblib`
- `scripts/matching_ia/output/matching_model_metadata.json`

## Selon quoi le modele est entraine

Le target `is_good_match` vaut:

- `1` si la paire joueur/equipe est consideree comme une bonne recommandation
- `0` sinon

Dans un vrai projet, ce label devrait venir de donnees reelles, par exemple:

- le joueur a rejoint l'equipe
- la candidature a ete acceptee
- le joueur est reste dans l'equipe
- l'equipe et le joueur avaient une bonne compatibilite observee

## Variables utilisees

- `player_role`
- `player_region`
- `player_level`
- `player_availability`
- `player_goal`
- `team_region`
- `team_level`
- `team_is_active`
- `team_is_private`
- `team_open_recruitments`
- `team_matching_role`
- `team_activity_score`
- `team_completion_rate`

## Modele choisi

Le script utilise un `RandomForestClassifier` car:

- simple a entrainer
- robuste sur des donnees mixtes
- bon point de depart pour un PFE / projet scolaire

Tu pourras ensuite remplacer ce modele par:

- `XGBoost`
- `LightGBM`
- un modele de ranking
- ou une API ML separee
