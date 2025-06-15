FROM python:3.10.4-slim

WORKDIR /app

RUN apt-get update && apt-get install -y gcc \
    && rm -rf /var/lib/apt/lists/*

COPY model_builder.py model_builder.py
COPY requirements.txt requirements.txt
COPY s3_utils.py s3_utils.py

RUN pip install --no-cache-dir -r requirements.txt

ENV AWS_ACCESS_KEY_ID="minioadmin"
ENV AWS_SECRET_ACCESS_KEY="minioadmin"
ENV S3_ENDPOINT_URL="http://recommendations-pipeline-minio:9000"
ENV S3_DATA_PATH="s3://data"
ENV S3_MODEL_PATH="s3://model"

CMD ["python", "-u", "model_builder.py"]