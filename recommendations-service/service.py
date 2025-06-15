import os

import numpy as np
from dotenv import load_dotenv
from fastapi import FastAPI
from lightfm import LightFM
from lightfm.data import Dataset
from pydantic import BaseModel

import s3_utils

load_dotenv()

fs = s3_utils.build_s3_filesystem()

s3_data_path = os.getenv("S3_DATA_PATH")
s3_model_path = os.getenv("S3_MODEL_PATH")

print("Loading lightfm_model")
model: LightFM = s3_utils.load_pickle_from_s3(fs, f"{s3_model_path}/lightfm_model.pkl")
print("Loading lightfm_dataset")
dataset: Dataset = s3_utils.load_pickle_from_s3(fs, f"{s3_model_path}/lightfm_dataset.pkl")
print("Loading lightfm_item_features")
item_features = s3_utils.load_npz_from_s3(fs, f"{s3_model_path}/lightfm_item_features.npz")
print("Loading anime")
anime = s3_utils.load_csv_from_s3(fs, f"{s3_data_path}/anime.csv")

anime_id_map, anime_id_inv_map = dataset.mapping()[2], {v: k for k, v in dataset.mapping()[2].items()}
user_id_map = dataset.mapping()[0]

print("Loading complete")

app = FastAPI()


class RecommendRequest(BaseModel):
    user_id: int
    num_items: int = 10


@app.post("/recommend")
def recommend(req: RecommendRequest):
    print(f'Request: {req}')

    if req.user_id not in user_id_map:
        print(f'User ID {req.user_id} not found in user_id_map')
        return []

    user_index = user_id_map[req.user_id]

    scores = model.predict(user_index, np.arange(len(anime_id_map)), item_features=item_features)
    top_items = np.argsort(-scores)[:req.num_items]

    result = []
    for idx in top_items:
        anime_id = anime_id_inv_map.get(idx)
        anime_row = anime[anime["anime_id"] == anime_id]
        if not anime_row.empty:
            row = anime_row.iloc[0]
            result.append({
                "anime_id": int(anime_id),
                "title": row["name"],
                "genres": row["genre"],
                "rating": row["rating"],
                "score": float(scores[idx])
            })
    print(f'Response: {result}')
    return result
