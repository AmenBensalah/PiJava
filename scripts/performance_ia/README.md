# Performance IA Training

Ce dossier contient un exemple Python pour entrainer l'IA de performance d'equipe.

## Objectif

Predire la classe de performance globale d'une equipe:

- `strong`
- `average`
- `weak`

## Variables utilisees

- `team_region`
- `team_level`
- `completed_tasks`
- `total_tasks`
- `delayed_tasks`
- `blocked_tasks`
- `active_members`
- `open_recruitments`
- `avg_workload`
- `team_morale`

## Installation

```bash
pip install pandas scikit-learn joblib
```

## Entrainement

```bash
python scripts/performance_ia/train_performance_model.py
```

## Sorties

- `scripts/performance_ia/output/performance_model.joblib`
- `scripts/performance_ia/output/performance_model_metadata.json`

## Selon quoi le modele est entraine

Le modele apprend a relier la performance finale de l'equipe avec:

- le volume de taches realisees
- les retards
- les blocages
- le nombre de membres actifs
- la charge moyenne
- le niveau global de l'equipe
- le moral ou climat estime

Le label final `performance_label` represente le niveau observe de performance.
