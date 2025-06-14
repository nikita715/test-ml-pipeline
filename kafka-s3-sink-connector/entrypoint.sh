#!/bin/bash

set -e

# Start Kafka Connect in the background
/etc/confluent/docker/run &

# Wait for Kafka Connect REST API to become available
until curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/connectors | grep -q "200"; do
  echo -e "\t$(date) Waiting for Kafka Connect to be ready..."
  sleep 5
done

# Set connector name (must match name field in JSON)
CONNECTOR_NAME="s3-sink-connector"

# Check if the connector already exists
EXISTS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/connectors/"$CONNECTOR_NAME")

if [ "$EXISTS" == "200" ]; then
  echo "🔄 Connector '$CONNECTOR_NAME' exists — updating config..."
  curl -X PUT -H "Content-Type: application/json" \
       --data @/app/s3-sink-put.json \
       http://localhost:8083/connectors/"$CONNECTOR_NAME"/config
else
  echo "🚀 Connector '$CONNECTOR_NAME' does not exist — creating it..."
  curl -X POST -H "Content-Type: application/json" \
       --data @/app/s3-sink-post.json \
       http://localhost:8083/connectors
fi

# Wait for Kafka Connect process to keep the container alive
wait
