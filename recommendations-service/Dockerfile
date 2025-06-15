FROM python:3.10.4-slim

WORKDIR /app

RUN apt-get update && apt-get install -y gcc \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt requirements.txt
COPY service.py service.py
COPY s3_utils.py s3_utils.py

RUN pip install --no-cache-dir -r requirements.txt

ENV AWS_ACCESS_KEY_ID="minioadmin"
ENV AWS_SECRET_ACCESS_KEY="minioadmin"
ENV S3_ENDPOINT_URL="http://recommendations-pipeline-minio:9000"
ENV S3_DATA_PATH="s3://data"
ENV S3_MODEL_PATH="s3://model"

EXPOSE 8000

CMD ["uvicorn", "service:app", "--host", "0.0.0.0", "--port", "8000"]