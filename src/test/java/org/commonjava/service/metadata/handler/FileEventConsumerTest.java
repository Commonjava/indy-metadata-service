package org.commonjava.service.metadata.handler;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.commonjava.event.file.FileEvent;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
public class FileEventConsumerTest
{

    @Inject
    @Any
    InMemoryConnector connector;

    @Test
    void testConsumeFileEvent()
    {
        InMemorySource<FileEvent> events = connector.source( "file-event-in");

        FileEvent fileEvent = new FileEvent();
        fileEvent.setSessionId( "build-0001" );

        // Use the send method to send a mock message to the events channel. So, our application will process this message.
        events.send(fileEvent);

        // TODO wait the app to process the event

        // TODO verify the result after processing the event

    }

}
