FROM python:3.10.4-slim

WORKDIR /app

RUN apt-get update && apt-get install -y gcc \
    && rm -rf /var/lib/apt/lists/*

COPY anime_recommends.py anime_recommends.py
COPY requirements.txt requirements.txt
COPY s3_utils.py s3_utils.py

RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "-u", "anime_recommends.py"]