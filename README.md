# RADAR-Connectors
Contains HDFS-Sink-Connector of RADAR-CNS platform

# To Run Radar-HDFS-Connector

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
    
# Contributing
Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html).
If you want to contribute a feature or fix browse our [issues](https://github.com/RADAR-CNS/RADAR-HDFS-Sink-Connector/issues), and please make a pull request.
    
