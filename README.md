# RADAR HDFS Sink connector

Contains HDFS-Sink-Connector of RADAR-base platform

## Direct usage

1. This connector depends on Kafka brokers, Kafka schema registry and an HDFS cluster.

2. Load the `radar-hdfs-sink-connector-*.jar` to CLASSPATH

    ```shell
    export CLASSPATH=/path/to/radar-hdfs-sink-connector-*.jar
    ```
      
3. Configure HDFS Connector properties. These particular settings will send invalid records to a dead letter queue.

    ```ini
    name=radar-hdfs-sink
    connector.class=io.confluent.connect.hdfs.HdfsSinkConnector
    tasks.max=4
    topics=test,test1,test2
    flush.size=16000000
    rotate.interval.ms=54000000
    hdfs.url=hdfs://hdfs-namenode:8020
    topics.dir=topicAndroidNew
    format.class=org.radarbase.sink.hdfs.AvroFormatRadar
    errors.tolerance=all
    errors.deadletterqueue.topic.name=dead_letter_queue_hdfs
    errors.deadletterqueue.topic.replication.factor=2
    errors.deadletterqueue.context.headers.enable=true
    errors.retry.delay.max.ms=60000
    errors.retry.timeout=300000
    ```
   
4. Run the connector. To run the connector in `standalone mode` (on an enviornment confluent platform is installed)
   
    ```shell
    connect-standalone /etc/schema-registry/connect-avro-standalone.properties path-to-your-hdfs-connector.properties
    ```

## Docker usage

To run this connector as a docker container, use the [radarbase/radar-hdfs-connector](https://hub.docker.org/radarbase/radar-hdfs-connector) docker image. See the README in the `docker` directory for more information.
It runs the [Confluent HDFS Connector 5.1.2]([here](https://docs.confluent.io/current/connect/kafka-connect-hdfs/index.html) using a custom record write provider to store both keys and values.

Create the docker image:
```
$ docker build -t radarbase/radar-hdfs-connector ./
```

Or pull from dockerhub:
```
$ docker pull radarbase/radar-hdfs-connector:1.0.0
```

### Configuration

This image has to be extended with a volume with appropriate `sink-hdfs.properties`, for example:

```ini
name=radar-hdfs-sink
connector.class=io.confluent.connect.hdfs.HdfsSinkConnector
tasks.max=4
topics=test,test1,test2
flush.size=16000000
rotate.interval.ms=54000000
hdfs.url=hdfs://hdfs-namenode:8020
topics.dir=topicAndroidNew
format.class=org.radarbase.sink.hdfs.AvroFormatRadar
errors.tolerance=all
errors.deadletterqueue.topic.name=dead_letter_queue_hdfs
errors.deadletterqueue.topic.replication.factor=2
errors.deadletterqueue.context.headers.enable=true
errors.retry.delay.max.ms=60000
errors.retry.timeout=300000
```

The docker-compose service could be defined as follows:

```yaml
services:
  radar-hdfs-connector:
    image: radarbase/radar-hdfs-connector:1.0.0
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
      test: ["CMD-SHELL", "curl  -sf localhost:8083/connectors/radar-hdfs-sink-15000/status | grep -o '\"state\":\"[^\"]*\"' | tr '\\n' ',' | grep -vq FAILED || exit 1"]
      interval: 1m
      timeout: 5s
      retries: 3
```

For a complete use case scenario, check the [RADAR-base `docker-compose` file](https://github.com/RADAR-base/RADAR-Docker/tree/master/dcompose-stack/radar-cp-hadoop-stack/docker-compose.yml).

## Contributing

Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html).
If you want to contribute a feature or fix browse our [issues](https://github.com/RADAR-base/RADAR-HDFS-Sink-Connector/issues), and please make a pull request.
