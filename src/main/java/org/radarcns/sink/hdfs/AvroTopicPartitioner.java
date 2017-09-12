package org.radarcns.sink.hdfs;

import io.confluent.connect.hdfs.partitioner.Partitioner;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AvroTopicPartitioner implements Partitioner {
    private static final String partitionField = "partition";
    private static final String keySchemaField = "key_schema_id";
    private static final String valueSchemaField = "value_schema_id";
    private final List<FieldSchema> partitionFields = new ArrayList<>(3);

    public void configure(Map<String, Object> config) {
        this.partitionFields.add(new FieldSchema(partitionField, TypeInfoFactory.stringTypeInfo.toString(), ""));
        this.partitionFields.add(new FieldSchema(keySchemaField, TypeInfoFactory.stringTypeInfo.toString(), ""));
        this.partitionFields.add(new FieldSchema(valueSchemaField, TypeInfoFactory.stringTypeInfo.toString(), ""));
    }

    public String encodePartition(SinkRecord record) {
        final Schema keySchema = record.keySchema();
        String keySchemaId = keySchema.name() + "-" + keySchema.version();
        final Schema valueSchema = record.valueSchema();
        String valueSchemaId = valueSchema.name() + "-" + valueSchema.version();

        return partitionField + "=" + record.kafkaPartition()
                + "&" + keySchemaField + "=" + keySchemaId
                + "&" + valueSchemaField + "=" + valueSchemaId;
    }

    public String generatePartitionedPath(String topic, String encodedPartition) {
        return topic + "/" + encodedPartition;
    }

    public List<FieldSchema> partitionFields() {
        return this.partitionFields;
    }
}
