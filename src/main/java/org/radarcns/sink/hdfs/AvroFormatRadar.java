/*
 * Copyright 2017 Kings College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.sink.hdfs;

import io.confluent.connect.avro.AvroData;
import io.confluent.connect.avro.AvroDataConfig;
import io.confluent.connect.hdfs.HdfsSinkConnectorConfig;
import io.confluent.connect.hdfs.avro.AvroFileReader;
import io.confluent.connect.hdfs.avro.AvroHiveFactory;
import io.confluent.connect.hdfs.storage.HdfsStorage;
import io.confluent.connect.storage.format.Format;
import io.confluent.connect.storage.format.RecordWriterProvider;
import io.confluent.connect.storage.format.SchemaFileReader;
import io.confluent.connect.storage.hive.HiveFactory;
import org.apache.hadoop.fs.Path;

import static io.confluent.connect.storage.StorageSinkConnectorConfig.SCHEMA_CACHE_SIZE_CONFIG;

/**
 * Extended AvroFormat class to support custom AvroRecordWriter to allow writting key and value to
 * HDFS.
 */
public class AvroFormatRadar implements Format<HdfsSinkConnectorConfig, Path> {
    private final AvroData avroData;

    public AvroFormatRadar(HdfsStorage storage) {
        AvroDataConfig avroConfig = new AvroDataConfig.Builder()
                .with(AvroDataConfig.CONNECT_META_DATA_CONFIG, false)
                .with(AvroDataConfig.SCHEMAS_CACHE_SIZE_CONFIG, storage.conf().getInt(SCHEMA_CACHE_SIZE_CONFIG))
                .with(AvroDataConfig.ENHANCED_AVRO_SCHEMA_SUPPORT_CONFIG, true)
                .build();

        this.avroData = new AvroData(avroConfig);
    }

    public RecordWriterProvider<HdfsSinkConnectorConfig> getRecordWriterProvider() {
        return new AvroKeyValueWriterProvider(this.avroData);
    }

    public SchemaFileReader<HdfsSinkConnectorConfig, Path> getSchemaFileReader() {
        return new AvroFileReader(this.avroData);
    }

    public HiveFactory getHiveFactory() {
        return new AvroHiveFactory(this.avroData);
    }
}
