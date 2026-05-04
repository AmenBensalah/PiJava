import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from pathlib import Path

# Charger les donnees
csv_path = Path(__file__).resolve().parent / "tournois_stats.csv"
df = pd.read_csv(csv_path)


def get_features_labels(dataframe):
    features = dataframe[[
        "nb_tournois",
        "types_tournoi_actifs",
        "types_jeu_actifs",
        "nb_solo",
        "nb_duo",
        "nb_squad",
        "nb_ligue",
        "nb_fps",
        "nb_sports",
        "nb_mind",
        "nb_battle_royale",
    ]]
    labels_type = dataframe["type_tournoi"]
    labels_jeu = dataframe["jeu"]
    return features, labels_type, labels_jeu


def afficher_statistiques(dataframe):
    print("\n=== Statistiques par type de tournoi ===")
    stats_type = dataframe["type_tournoi"].value_counts().sort_values(ascending=False)
    stats_type_pct = (dataframe["type_tournoi"].value_counts(normalize=True) * 100).round(2)
    for type_tournoi, count in stats_type.items():
        print(f"- {type_tournoi}: {count} ({stats_type_pct[type_tournoi]}%)")

    print("\n=== Statistiques par type de jeu ===")
    stats_type_jeu = dataframe["type_jeu"].value_counts().sort_values(ascending=False)
    stats_type_jeu_pct = (dataframe["type_jeu"].value_counts(normalize=True) * 100).round(2)
    for type_jeu, count in stats_type_jeu.items():
        print(f"- {type_jeu}: {count} ({stats_type_jeu_pct[type_jeu]}%)")

    print("\n=== Statistiques croisees (type tournoi x type jeu) ===")
    tableau_croise = pd.crosstab(dataframe["type_tournoi"], dataframe["type_jeu"])
    print(tableau_croise)


X, y_type, y_jeu = get_features_labels(df)

# Separer en train/test
X_train, _, y_type_train, _ = train_test_split(X, y_type, test_size=0.2, random_state=42)
_, _, y_jeu_train, _ = train_test_split(X, y_jeu, test_size=0.2, random_state=42)

# Modele pour type de tournoi
clf_type = RandomForestClassifier(random_state=42)
clf_type.fit(X_train, y_type_train)

# Modele pour jeu
clf_jeu = RandomForestClassifier(random_state=42)
clf_jeu.fit(X_train, y_jeu_train)

# Exemple de prediction (a remplacer par de vraies valeurs)
nouvelle_stat = pd.DataFrame([[3, 3, 2, 1, 1, 1, 1, 2, 1, 0, 0]], columns=X.columns)
type_pred = clf_type.predict(nouvelle_stat)
jeu_pred = clf_jeu.predict(nouvelle_stat)

print(f"Type de tournoi predit : {type_pred[0]}")
print(f"Jeu predit : {jeu_pred[0]}")

afficher_statistiques(df)
