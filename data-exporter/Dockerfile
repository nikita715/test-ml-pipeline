FROM gradle:8.10-jdk21 AS build

WORKDIR /app
COPY . .

RUN gradle clean shadowJar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV AWS_ACCESS_KEY_ID="minioadmin"
ENV AWS_SECRET_ACCESS_KEY="minioadmin"
ENV S3_ENDPOINT_URL="http://recommendations-pipeline-minio:9000"

ENTRYPOINT ["java", "-jar", "app.jar"]
