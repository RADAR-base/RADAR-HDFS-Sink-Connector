# RADAR HDFS Sink connector
Contains HDFS-Sink-Connector of RADAR-base platform

## Direct usage

1. In addition to Zookeeper, Kafka-broker(s), Schema-registry and Rest-proxy, HDFS should be running
2. Load the `radar-hdfs-sink-connector-*.jar` to CLASSPATH

    ```shell
    export CLASSPATH=/path/to/radar-hdfs-sink-connector-*.jar
    ```
      
3. Configure HDFS Connector properties.

    ```ini
    name=radar-hdfs-sink
    connector.class=io.confluent.connect.hdfs.HdfsSinkConnector
    tasks.max=1
    topics=mock_empatica_e4_battery_level,mock_empatica_e4_blood_volume_pulse
    flush.size=1200
    hdfs.url=hdfs://localhost:9000
    format.class=org.radarcns.sink.hdfs.AvroFormatRadar
    partitioner.class=org.radarcns.sink.hdfs.AvroTopicPartitioner
    ```
   
4. Run the connector. To run the connector in `standalone mode` (on an enviornment confluent platform is installed)
   
    ```shell
    connect-standalone /etc/schema-registry/connect-avro-standalone.properties path-to-your-hdfs-connector.properties
    ```

## Docker usage

To run this connector as a docker container, use the [radarbase/radar-hdfs-connector](https://hub.docker.org/radarbase/radar-hdfs-connector) docker image. See the README in the `docker` directory for more information.
It runs the Confluent HDFS Connector 4.1.0 using a custom [RecordWriterProvider](https://github.com/RADAR-CNS/RADAR-Backend/blob/dev/src/main/java/org/radarcns/sink/hdfs/AvroRecordWriterProviderRadar.java) to support RADAR-CNS Avro schemas. For more details about Confluent HDFS Connector click [here](http://docs.confluent.io/4.1.0/connect/connect-hdfs/docs/index.html).

Create the docker image:
```
$ docker build -t radarbase/radar-hdfs-connector ./
```

Or pull from dockerhub:
```
$ docker pull radarbase/radar-hdfs-connector:0.1.2
```

### Configuration

This image has to be extended with a volume with appropriate `sink-hdfs.properties`, for example:

```ini
name=radar-hdfs-sink-15000
connector.class=io.confluent.connect.hdfs.HdfsSinkConnector
tasks.max=4
topics=topic1, topic2, ...
flush.size=15000
hdfs.url=hdfs://namenode:8020
format.class=org.radarcns.sink.hdfs.AvroFormatRadar
topics.dir=topicAndroidNew
```

The docker-compose service could be defined as follows:

```yaml
services:
  radar-hdfs-connector:
    image: radarbase/radar-hdfs-connector:0.1.2
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

For a complete use case scenario, check the [RADAR-CNS `docker-compose` file](https://github.com/RADAR-CNS/RADAR-Docker/blob/backend-integration/dcompose-stack/radar-cp-hadoop-stack/docker-compose.yml).

## Contributing

Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html).
If you want to contribute a feature or fix browse our [issues](https://github.com/RADAR-base/RADAR-HDFS-Sink-Connector/issues), and please make a pull request.
