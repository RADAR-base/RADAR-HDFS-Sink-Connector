package org.radarcns.sink.hdfs;

import io.confluent.connect.hdfs.partitioner.FieldPartitioner;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AvroTopicPartitioner extends FieldPartitioner {
    private static final String PARTITION_FIELD = "partition";
    private static final String KEY_SCHEMA_FIELD = "key_schema";
    private static final String VALUE_SCHEMA_FIELD = "value_schema";
    private final List<FieldSchema> hiveFields = new ArrayList<>(3);

    @Override
    public void configure(Map<String, Object> config) {
        String stringType = TypeInfoFactory.stringTypeInfo.toString();
        this.hiveFields.add(new FieldSchema(PARTITION_FIELD, stringType, ""));
        this.hiveFields.add(new FieldSchema(KEY_SCHEMA_FIELD, stringType, ""));
        this.hiveFields.add(new FieldSchema(VALUE_SCHEMA_FIELD, stringType, ""));
    }

    @Override
    public String encodePartition(SinkRecord record) {
        final Schema keySchema = record.keySchema();
        String keySchemaId;
        if (keySchema == null) {
            keySchemaId = "null";
        } else {
            keySchemaId = keySchema.name() + "-" + keySchema.version();
        }
        final Schema valueSchema = record.valueSchema();
        String valueSchemaId;
        if (valueSchema == null) {
            valueSchemaId = "null";
        } else {
            valueSchemaId = valueSchema.name() + "-" + valueSchema.version();
        }

        return PARTITION_FIELD + "=" + record.kafkaPartition()
                + "_" + KEY_SCHEMA_FIELD + "=" + keySchemaId
                + "_" + VALUE_SCHEMA_FIELD + "=" + valueSchemaId;
    }

    @Override
    public String generatePartitionedPath(String topic, String encodedPartition) {
        return topic + "/" + encodedPartition;
    }

    @Override
    public List<FieldSchema> partitionFields() {
        return this.hiveFields;
    }
}
