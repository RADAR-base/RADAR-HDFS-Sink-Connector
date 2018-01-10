# Dockerised RADAR-HDFS-Connector

It runs the Confluent HDFS Connector 3.2.1 using a custom [RecordWriterProvider](https://github.com/RADAR-CNS/RADAR-Backend/blob/dev/src/main/java/org/radarcns/sink/hdfs/AvroRecordWriterProviderRadar.java) to support RADAR-CNS Avro schemas. For more details about Confluent HDFS Connector click [here](http://docs.confluent.io/3.1.2/connect/connect-hdfs/docs/index.html).

Create the docker image:
```
$ docker build -t radarcns/radar-hdfs-connector ./
```

Or pull from dockerhub:
```
$ docker pull radarcns/radar-hdfs-connector:0.1
```

## Configuration

This image has to be extended with a volume with appropriate `sink-hdfs.properties`, for example:

```ini
name=radar-hdfs-sink-android-15000
connector.class=io.confluent.connect.hdfs.HdfsSinkConnector
tasks.max=4
topics=topic1, topic2, ...
flush.size=15000
hdfs.url=hdfs://namenode:8020
format.class=org.radarcns.sink.hdfs.AvroFormatRadar
topics.dir=topicAndroidNew
partitioner.class=org.radarcns.sink.hdfs.AvroTopicPartitioner
```

The docker-compose service could be defined as follows:

```yaml
services:
  radar-hdfs-connector:
    image: radarcns/radar-hdfs-connector:0.1
    restart: on-failure
    volumes:
      - ./sink-hdfs.properties:/etc/kafka-connect/sink-hdfs.properties
    depends_on:
      - zookeeper-1
      - kafka-1
      - kafka-2
      - kafka-3
      - schema-registry-1
      - hdfs-namenode
    environment:
      CONNECT_BOOTSTRAP_SERVERS: PLAINTEXT://kafka-1:9092,PLAINTEXT://kafka-2:9092,PLAINTEXT://kafka-3:9092
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: "default"
      CONNECT_CONFIG_STORAGE_TOPIC: "default.config"
      CONNECT_OFFSET_STORAGE_TOPIC: "default.offsets"
      CONNECT_STATUS_STORAGE_TOPIC: "default.status"
      CONNECT_KEY_CONVERTER: "io.confluent.connect.avro.AvroConverter"
      CONNECT_VALUE_CONVERTER: "io.confluent.connect.avro.AvroConverter"
      CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL: "http://schema-registry-1:8081"
      CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL: "http://schema-registry-1:8081"
      CONNECT_INTERNAL_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_OFFSET_STORAGE_FILE_FILENAME: "/tmp/connect2.offset"
      CONNECT_REST_ADVERTISED_HOST_NAME: "radar-hdfs-connector"
      CONNECT_ZOOKEEPER_CONNECT: zookeeper-1:2181
      CONNECTOR_PROPERTY_FILE_PREFIX: "sink-hdfs"
      KAFKA_HEAP_OPTS: "-Xms256m -Xmx768m"
      KAFKA_BROKERS: 3
    healthcheck:
      test: ["CMD-SHELL", "curl  -sf localhost:8083/connectors/radar-hdfs-sink-android-15000/status | grep -o '\"state\":\"[^\"]*\"' | tr '\\n' ',' | grep -vq FAILED || exit 1"]
      interval: 1m
      timeout: 5s
      retries: 3
```

For a complete use case scenario, check the [RADAR-CNS `docker-compose` file](https://github.com/RADAR-CNS/RADAR-Docker/blob/backend-integration/dcompose-stack/radar-cp-hadoop-stack/docker-compose.yml).
