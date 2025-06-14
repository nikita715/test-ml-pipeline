FROM confluentinc/cp-kafka-connect:7.4.0

WORKDIR /app

COPY entrypoint.sh .
COPY s3-sink-post.json .
COPY s3-sink-put.json .

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-s3:10.5.7

EXPOSE 8083:8083

CMD /app/entrypoint.sh