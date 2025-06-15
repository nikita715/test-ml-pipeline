import os

import pandas as pd
from dotenv import load_dotenv
from lightfm import LightFM
from lightfm.data import Dataset
from sklearn.preprocessing import MultiLabelBinarizer

from s3_utils import load_csv_from_s3, build_s3_filesystem, save_pickle_to_s3, save_npz_to_s3, \
    load_and_combine_parquet_files

load_dotenv()

fs = build_s3_filesystem()

s3_data_path = os.getenv("S3_DATA_PATH")

print("Loading anime and ratings data")
anime = load_csv_from_s3(fs, f"{s3_data_path}/anime.csv")
ratings = load_and_combine_parquet_files(fs)

print("Preparing data")
anime = anime.dropna(subset=['name'])
ratings = ratings[ratings['rating'] != -1]

anime['genre'] = anime['genre'].fillna('Unknown')
anime['genre_list'] = anime['genre'].str.split(', ')

mlb = MultiLabelBinarizer()
genre_encoded = pd.DataFrame(mlb.fit_transform(anime['genre_list']), columns=mlb.classes_)
anime = pd.concat([anime[['anime_id', 'name', 'type', 'episodes', 'members']], genre_encoded], axis=1)

anime['episodes'] = pd.to_numeric(anime['episodes'], errors='coerce').fillna(1).astype(int)
anime['members'] = pd.to_numeric(anime['members'], errors='coerce').fillna(0).astype(int)

ratings = ratings[ratings["animeId"].isin(anime["anime_id"])]

all_item_features = set()

for _, row in anime.iterrows():
    all_item_features.add(f"type:{row['type']}")
    all_item_features.add(f"episodes_bin:{int(row['episodes'] // 10 * 10)}")
    all_item_features.add(f"members_bin:{int(row['members'] // 10000 * 10000)}")
    for genre in mlb.classes_:
        if row.get(genre, 0) == 1:
            all_item_features.add(f"genre:{genre}")

dataset = Dataset()
dataset.fit(
    users=ratings['userId'].unique(),
    items=anime['anime_id'].unique(),
    item_features=all_item_features
)


def build_features(row):
    feats = []
    feats.append(f"type:{row['type']}")
    feats.append(f"episodes_bin:{int(row['episodes'] // 10 * 10)}")
    feats.append(f"members_bin:{int(row['members'] // 10000 * 10000)}")
    for genre in mlb.classes_:
        if row.get(genre, 0) == 1:
            feats.append(f"genre:{genre}")
    return (row['anime_id'], feats)


anime_features = [build_features(row) for _, row in anime.iterrows()]
item_features = dataset.build_item_features(anime_features)

print("Building interactions")
interactions, _ = dataset.build_interactions([
    (row['userId'], row['animeId'], row['rating']) for _, row in ratings.iterrows()
])

print("Fitting model")
model = LightFM(loss='warp')
model.fit(interactions, item_features=item_features, epochs=20, num_threads=2)

print("Saving files")
s3_model_path = os.getenv("S3_MODEL_PATH")

save_pickle_to_s3(fs, model, f"{s3_model_path}/lightfm_model.pkl")
save_pickle_to_s3(fs, dataset, f"{s3_model_path}/lightfm_dataset.pkl")
save_npz_to_s3(fs, item_features, f"{s3_model_path}/lightfm_item_features.npz")

print("End")
