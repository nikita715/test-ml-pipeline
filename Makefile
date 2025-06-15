include .env

all: start-minikube build-data-exporter build-event-generator build-kafka-s3-sink-connector build-model-builder build-recommendations-service helm-update-dependencies helm-upgrade

start-minikube:
	minikube start

build-data-exporter:
	eval $$(minikube -p minikube docker-env) && \
	docker build -t nikita715/data-exporter:latest --no-cache ./data-exporter

build-event-generator:
	eval $$(minikube -p minikube docker-env) && \
	docker build -t nikita715/event-generator:latest ./event-generator

build-kafka-s3-sink-connector:
	eval $$(minikube -p minikube docker-env) && \
	docker build -t nikita715/kafka-s3-sink-connector:latest --no-cache ./kafka-s3-sink-connector

build-model-builder:
	eval $$(minikube -p minikube docker-env) && \
	docker build -t nikita715/model-builder:latest --no-cache ./model-builder

build-recommendations-service:
	eval $$(minikube -p minikube docker-env) && \
	docker build -t nikita715/recommendations-service:latest --no-cache -f ./model-builder/Dockerfile-service ./model-builder

helm-update-dependencies:
	helm dependency update ./k8s/recommendations-pipeline

helm-upgrade:
	helm upgrade --install recommendations-pipeline ./k8s/recommendations-pipeline

open-recommendations-service-port:
	minikube service recommendations-service --url
