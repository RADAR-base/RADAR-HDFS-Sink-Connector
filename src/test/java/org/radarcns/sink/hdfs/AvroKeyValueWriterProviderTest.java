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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.confluent.connect.avro.AvroData;
import io.confluent.connect.hdfs.HdfsSinkConnectorConfig;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.confluent.connect.storage.format.RecordWriter;
import org.apache.avro.file.DataFileWriter;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.Before;
import org.junit.Test;

public class AvroKeyValueWriterProviderTest {
    private AvroKeyValueWriterProvider provider;
    private HdfsSinkConnectorConfig conf;
    private String outputFile;

    @Before
    public void setUp() throws IOException {
        outputFile = File.createTempFile("AvroTest", null).getAbsolutePath();
        Map<String, String> props = new HashMap<>();
        props.put(HdfsSinkConnectorConfig.FLUSH_SIZE_CONFIG,"15000");
        conf = new HdfsSinkConnectorConfig(props);
        AvroData avroData = new AvroData(100);
        provider = new AvroKeyValueWriterProvider(avroData);
    }

    @Test
    public void recordWriter() throws Exception {
        SinkRecord record = new SinkRecord("mine", 0, null, null,
                SchemaBuilder.string().build(), "hi", 0);
        RecordWriter writer = provider.getRecordWriter(
                conf, outputFile);
        writer.write(record);
        writer.write(new SinkRecord("mine", 0, null, "withData",
                SchemaBuilder.string().build(), "hi", 0));
        writer.close();
        assertTrue(0 < outputFile.length());
    }

    @Test(expected = DataFileWriter.AppendWriteException.class)
    public void recordWriterWrongSchema() throws Exception {
        SinkRecord record = new SinkRecord("mine", 0, SchemaBuilder.string().build(),
                "something", SchemaBuilder.string().build(), "hi", 0);
        RecordWriter writer = provider.getRecordWriter(
                conf, outputFile);
        writer.write(record);
        writer.write(new SinkRecord("mine", 0, null, null,
                    SchemaBuilder.string().build(), "hi", 0));
        writer.close();
        assertEquals(0, outputFile.length());
    }
}