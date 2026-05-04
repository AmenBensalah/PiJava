import pandas as pd
import pickle
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
    print(f'Trained recommender with {len(user_item_matrix)} users and {len(user_item_matrix.columns)} posts.')


if __name__ == '__main__':
    train_and_save_model()
