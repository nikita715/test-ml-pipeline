import io
import os
import pickle

import pandas as pd
import s3fs
from s3fs import S3FileSystem
from scipy.sparse import load_npz, save_npz


def build_s3_filesystem():
    s3_endpoint = os.getenv("S3_ENDPOINT_URL")
    s3_access_key = os.getenv("AWS_ACCESS_KEY_ID")
    s3_secret_key = os.getenv("AWS_SECRET_ACCESS_KEY")

    return s3fs.S3FileSystem(
        key=s3_access_key,
        secret=s3_secret_key,
        client_kwargs={"endpoint_url": s3_endpoint}
    )


def load_csv_from_s3(fs: S3FileSystem, path: str):
    with fs.open(path, 'rb', encoding='utf-8') as f:
        return pd.read_csv(f)


def save_pickle_to_s3(fs: S3FileSystem, obj, path: str):
    buffer = io.BytesIO()
    pickle.dump(obj, buffer)
    buffer.seek(0)
    with fs.open(path, 'wb') as f:
        return f.write(buffer.read())


def load_pickle_from_s3(fs: S3FileSystem, path: str):
    with fs.open(path, 'rb') as f:
        return pickle.load(f)


def save_npz_to_s3(fs: S3FileSystem, obj, path: str):
    with fs.open(path, 'wb') as f:
        save_npz(f, obj)


def load_npz_from_s3(fs: S3FileSystem, path: str):
    with fs.open(path, 'rb') as f:
        return load_npz(f)
