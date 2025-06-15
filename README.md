# 📺 TV Show Recommendations Pipeline

A proof-of-concept MLOps pipeline for TV show recommendations based on the flow of user ratings, using microservices, real-time event streaming, automated model training (LightFM), and scalable deployment with Kubernetes.

---

## 🧩 System Overview

- **`data-exporter`**  
  Periodically exports TV show metadata to object storage (MinIO).

- **`event-generator`**  
  Continuously generates user rating events and sends them to Apache Kafka.

- **`Kafka + Kafka S3 Sink Connector`**  
  Consumes rating events and stores them in batches in MinIO.

- **`model-builder`**  
  Runs every 2 minutes, downloads user ratings and TV show metadata from MinIO, trains a machine learning model, and uploads the latest model back to MinIO.

- **`recommendations-service`**  
  Loads the latest model from MinIO and provides REST API for TV show recommendations.

---

## 🔧 Technologies Used

- **Kotlin**
- **Python**, **FastAPI**
- **Kubernetes**, **Helm** - Container orchestration
- **Apache Kafka**, **Schema Registry**, **S3 Sink Connector** - Event processing
- **MinIO** - S3 Storage
- **lightfm** - Model training
---

## ⚙️ Installation

### Prerequisites

- [Docker](https://www.docker.com/get-started) — Container runtime
- [kubectl](https://kubernetes.io/ru/docs/tasks/tools/install-kubectl/) — Kubernetes CLI
- [Helm](https://helm.sh/docs/intro/install/) — Kubernetes package manager
- [Minikube](https://minikube.sigs.k8s.io/docs/start/) — Local Kubernetes cluster

### Startup

To build and start the entire system, run:

```bash
make all
```

### Testing

To test the recommendations-service, first expose its port by running:

```bash
make open-recommendations-service-port
```

Then send a recommendation request with some `userId` to your opened port, for example:

```bash
curl --location 'http://127.0.0.1:65388/recommend' \
--header 'Content-Type: application/json' \
--data '{
    "user_id": 1,
    "num_items": 10
}'
```

---

## 📐 Architecture Diagram
<img src="docs/architecture.svg" alt="Architecture">