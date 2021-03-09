package org.commonjava.service.metadata.handler;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;
import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.cache.infinispan.CacheProducer;
import org.commonjava.service.metadata.config.ISPNConfiguration;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import static org.commonjava.service.metadata.handler.MetadataUtil.getMetadataPath;

@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
public class FileEventConsumerTest
{

    @Inject
    MetadataCacheManager cacheManager;

    @Inject
    @Any
    InMemoryConnector connector;

    @Test
    void testConsumeFileEvent()
    {
        String storeKey = "maven:hosted:build-0001";
        String path = "org/commomjava/test-1.pom";
        String mdPath = getMetadataPath( path );
        MetadataKey key = new MetadataKey( StoreKey.fromString( storeKey ), mdPath );
        cacheManager.put( key, new MetadataInfo( null ) );
        MetadataInfo result = cacheManager.get( key );
        Assertions.assertNotNull( result );

        InMemorySource<FileEvent> events = connector.source( "file-event-in");

        FileEvent fileEvent = new FileEvent();
        fileEvent.setTargetPath( path );
        fileEvent.setStoreKey( storeKey );
        fileEvent.setEventType( FileEventType.STORAGE );
        fileEvent.setSessionId( "build-0001" );

        // Use the send method to send a mock message to the events channel. So, our application will process this message.
        events.send(fileEvent);

        // Check if the metadata file had been removed from the cache.
        result = cacheManager.get( key );
        Assertions.assertNull( result );

    }

}
