include .env

start-minikube:
	minikube start

build-event-generator:
	eval $$(minikube -p minikube docker-env)
	docker build -t nikita715/event-generator:latest --no-cache ./event-generator

build-kafka-s3-sink-connector:
	eval $$(minikube -p minikube docker-env)
	docker build -t nikita715/kafka-s3-sink-connector:latest --no-cache ./kafka-s3-sink-connector

build-model-builder:
	eval $$(minikube -p minikube docker-env)
	docker build -t nikita715/model-builder:latest --no-cache ./model-builder

build-recommendation-service:
	eval $$(minikube -p minikube docker-env)
	docker build -t nikita715/recommendations-service:latest --no-cache -f ./model-builder/Dockerfile-service ./model-builder

helm-update-dependencies:
	helm dependency update ./k8s/recommendations-service

helm-upgrate:
	helm upgrade --install recommendations-pipeline ./k8s/recommendations-service
