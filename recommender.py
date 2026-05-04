import os
import sys
import json
import pickle
import pandas as pd
from sklearn.neighbors import NearestNeighbors

MODEL_FILE = 'recommender_model.pkl'
INTERACTIONS_FILE = 'user_interactions.csv'


def load_data():
    return pd.read_csv(INTERACTIONS_FILE)


def train_and_save_model():
    data = load_data()
    user_item_matrix = data.pivot(index='user_id', columns='post_id', values='rating').fillna(0)
    model = NearestNeighbors(metric='cosine', algorithm='brute')
    model.fit(user_item_matrix)
    with open(MODEL_FILE, 'wb') as f:
        pickle.dump({'model': model, 'user_item_matrix': user_item_matrix}, f)
    return model, user_item_matrix


def load_model():
    if os.path.exists(MODEL_FILE):
        with open(MODEL_FILE, 'rb') as f:
            data = pickle.load(f)
            return data['model'], data['user_item_matrix']
    return train_and_save_model()


def recommend_posts(user_id, num_recommendations=5):
    model, user_item_matrix = load_model()
    if user_id not in user_item_matrix.index:
        return []

    user_vector = user_item_matrix.loc[user_id].values.reshape(1, -1)
    distances, indices = model.kneighbors(user_vector, n_neighbors=min(num_recommendations + 1, len(user_item_matrix)))

    similar_users = indices.flatten()[1:]
    recommended_posts = []
    for similar_user in similar_users:
        user_posts = user_item_matrix.iloc[similar_user]
        top_posts = user_posts[user_posts > 0].index.tolist()
        recommended_posts.extend(top_posts)

    recommended_posts = list(dict.fromkeys(recommended_posts))[:num_recommendations]
    return recommended_posts


if __name__ == '__main__':
    if len(sys.argv) > 1 and sys.argv[1] == '--train':
        model, matrix = train_and_save_model()
        print(f'Trained recommender with {len(matrix)} users and {len(matrix.columns)} posts.')
    elif len(sys.argv) > 1:
        try:
            user_id = int(sys.argv[1])
            recommendations = recommend_posts(user_id, 5)
            print(json.dumps(recommendations))
        except ValueError:
            print(json.dumps([]))
    else:
        print(json.dumps([]))