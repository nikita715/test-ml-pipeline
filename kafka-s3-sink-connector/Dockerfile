FROM confluentinc/cp-kafka-connect:7.4.0

WORKDIR /app

COPY entrypoint.sh .
COPY s3-sink-post.json .
COPY s3-sink-put.json .

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-s3:10.5.7

ENV CONNECT_BOOTSTRAP_SERVERS="recommendations-pipeline-kafka:9092"
ENV CONNECT_REST_PORT="8083"
ENV CONNECT_GROUP_ID="kafka-connect-group"
ENV CONNECT_CONFIG_STORAGE_TOPIC="_connect-configs"
ENV CONNECT_OFFSET_STORAGE_TOPIC="_connect-offsets"
ENV CONNECT_STATUS_STORAGE_TOPIC="_connect-status"
ENV CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR="1"
ENV CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR="1"
ENV CONNECT_STATUS_STORAGE_REPLICATION_FACTOR="1"
ENV CONNECT_KEY_CONVERTER="io.confluent.connect.avro.AvroConverter"
ENV CONNECT_VALUE_CONVERTER="io.confluent.connect.avro.AvroConverter"
ENV CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL="http://recommendations-pipeline-schema-registry:8081"
ENV CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL="http://recommendations-pipeline-schema-registry:8081"
ENV CONNECT_INTERNAL_KEY_CONVERTER="org.apache.kafka.connect.json.JsonConverter"
ENV CONNECT_INTERNAL_VALUE_CONVERTER="org.apache.kafka.connect.json.JsonConverter"
ENV CONNECT_PLUGIN_PATH="/usr/share/confluent-hub-components"
ENV CONNECT_REST_ADVERTISED_HOST_NAME="localhost"

EXPOSE 8083:8083

CMD /app/entrypoint.sh