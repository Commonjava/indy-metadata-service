/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.metadata.client;

import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;

public class SimpleProducer {

    public static void main(String[] args) throws Exception
    {

        String topicName = "file-event";

        Properties props = new Properties();

        props.put(ProducerConfig.CLIENT_ID_CONFIG, "Simple producer");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.commonjava.service.metadata.client.FileEventSerializer");

        Producer<String, FileEvent> producer = new KafkaProducer(props);

        for(int i = 0; i < 10; i++)
        {
            FileEvent event = new FileEvent(FileEventType.STORAGE);
            event.setSessionId(String.valueOf(i));
            event.setTargetPath("io/enmasse/keycloak-user-api/0.34.0.test-0001/keycloak-user-api-0.34.0.test-0001.pom");
            event.setStoreKey("maven:hosted:build-0001");
            producer.send(new ProducerRecord(topicName,
                    String.valueOf(i), event ));

        }
        System.out.println("Message sent successfully");
        producer.close();
    }
}
