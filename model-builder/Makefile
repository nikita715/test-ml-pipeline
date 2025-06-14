include .env

build:
	docker build -t nikita715/model-builder:latest --no-cache .

run:
	docker run -d --rm --env-file .env nikita715/model-builder:latest