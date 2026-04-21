import pandas as pd
import numpy as np
from flask import Flask, jsonify, request
import mysql.connector

app = Flask(__name__)

# Configuration de la base de données
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root', # Modifier si vous avez un utilisateur spécifique
    'password': '', # Modifier si vous avez un mot de passe
    'database': 'esportify'
}

def get_db_connection():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"Erreur de connexion à la base de données: {e}")
        return None

def train_popularity_model():
    """
    Simule ou entraîne un modèle de Machine Learning basé sur la popularité (les plus vendus).
    """
    conn = get_db_connection()
    if not conn:
        return {"error": "Impossible de se connecter à la base de données."}
        
    try:
        # 1. Récupérer les produits depuis la base de données
        query_products = "SELECT id, nom, prix, stock, categorieId FROM produit"
        products_df = pd.read_sql(query_products, conn)
        
        if products_df.empty:
            return []

        # 2. Récupération des ventes (Simulation s'il n'y a pas de table "commande" ou "transaction")
        # En ML réel, on ferait:
        # query_sales = "SELECT produit_id, SUM(quantite) as ventes FROM ligne_commande GROUP BY produit_id"
        # sales_df = pd.read_sql(query_sales, conn)
        # products_df = pd.merge(products_df, sales_df, left_on='id', right_on='produit_id', how='left').fillna(0)
        
        # === SIMULATION ML (Génération de fausses données de vente pour le modèle) ===
        np.random.seed(42) # Pour avoir des résultats constants
        # On donne un avantage aléatoire à certains produits pour simuler "les plus vendus"
        products_df['ventes_totales'] = np.random.poisson(lam=20, size=len(products_df))
        
        # 3. Feature Engineering & Modèle de score
        # On favorise les produits les plus vendus, et on pénalise ceux en rupture de stock
        products_df['disponible'] = (products_df['stock'] > 0).astype(int)
        
        # Le Score ML de recommandation = Ventes (80%) + Disponibilité (20%)
        # On applique une normalisation simple (MinMaxScaler concept)
        max_ventes = products_df['ventes_totales'].max() if products_df['ventes_totales'].max() > 0 else 1
        products_df['score_recommandation'] = ((products_df['ventes_totales'] / max_ventes) * 80) + (products_df['disponible'] * 20)
        
        # 4. Trier par le meilleur score pour obtenir les recommandations
        recommended_df = products_df.sort_values(by='score_recommandation', ascending=False)
        
        # On prend le Top 5 des recommandations
        top_recommendations = recommended_df.head(5)
        
        # Convertir en liste de dictionnaires pour le JSON
        result = top_recommendations[['id', 'nom', 'prix', 'ventes_totales', 'score_recommandation']].to_dict(orient='records')
        return result

    except Exception as e:
        return {"error": str(e)}
    finally:
        if conn and conn.is_connected():
            conn.close()

@app.route('/api/recommendations', methods=['GET'])
def get_recommendations():
    """
    Endpoint (API REST) pour que JavaFX puisse récupérer les recommandations
    """
    recommandations = train_popularity_model()
    
    if isinstance(recommandations, dict) and "error" in recommandations:
        return jsonify({"status": "error", "message": recommandations["error"]}), 500
        
    return jsonify({
        "status": "success",
        "message": "Modèle ML : Recommandation des produits les plus vendus",
        "data": recommandations
    })

if __name__ == '__main__':
    print("🚀 API de Recommandation ML démarrée sur http://localhost:5000")
    print("Appelez http://localhost:5000/api/recommendations pour voir les résultats.")
    app.run(debug=True, port=5000)
