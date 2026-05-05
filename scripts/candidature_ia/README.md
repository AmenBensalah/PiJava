# Candidature IA Training

Ce dossier contient un exemple Python pour entrainer l'IA qui aide a classer ou accepter les candidatures.

## Objectif

Predire si une candidature a de bonnes chances d'etre acceptee.

## Variables utilisees

- `candidate_region`
- `candidate_level`
- `candidate_role`
- `candidate_availability`
- `candidate_motivation_score`
- `team_region`
- `team_level`
- `team_needed_role`
- `team_open_slots`
- `team_role_balance_score`
- `team_activity_score`

## Label

- `is_accepted`
  - `1` = candidature jugee compatible / acceptee
  - `0` = candidature jugee faible / refusee

## Installation

```bash
pip install pandas scikit-learn joblib
```

## Entrainement

```bash
python scripts/candidature_ia/train_candidature_model.py
```

## Sorties

- `scripts/candidature_ia/output/candidature_model.joblib`
- `scripts/candidature_ia/output/candidature_model_metadata.json`

## Selon quoi le modele est entraine

Le modele apprend selon:

- adequation region joueur / equipe
- adequation niveau joueur / equipe
- role cherche par l'equipe
- disponibilite du joueur
- score de motivation
- places encore disponibles
- equilibre actuel du roster
- activite recente de l'equipe
