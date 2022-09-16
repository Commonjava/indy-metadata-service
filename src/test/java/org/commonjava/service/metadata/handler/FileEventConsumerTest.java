package org.commonjava.service.metadata.handler;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;
import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import static org.commonjava.service.metadata.client.RepositoryServiceTest.AFFECTED_GROUPS;
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

    String storeKey = "maven:hosted:build-0001";
    String path = "org/commomjava/test-1.pom";
    MetadataKey key;
    MetadataKey groupKey;

    @BeforeEach
    void init()
    {
        String mdPath = getMetadataPath( path );
        key = new MetadataKey( StoreKey.fromString( storeKey ), mdPath );
        cacheManager.put( key, new MetadataInfo( null ) );

        groupKey = new MetadataKey( StoreKey.fromString( AFFECTED_GROUPS ), mdPath );
        cacheManager.put( groupKey, new MetadataInfo( null ) );
    }

    // @Test
    void testConsumeFileStoreEvent()
    {

        MetadataInfo result = cacheManager.get( key );
        Assertions.assertNotNull( result );

        MetadataInfo groupResult = cacheManager.get( groupKey );
        Assertions.assertNotNull( groupResult );

        FileEvent fileEvent = buildFileEvent( new FileEvent( FileEventType.STORAGE ) );

        // Use the send method to send a mock message to the events channel. So, our application will process this message.
        emit( fileEvent );

        // Check if the metadata file had been removed from the cache.
        result = cacheManager.get( key );
        Assertions.assertNull( result );

        groupResult = cacheManager.get( groupKey );
        Assertions.assertNull( groupResult );

    }

    //@Test
    void testConsumeFileDeleteEvent()
    {

        MetadataInfo result = cacheManager.get( key );
        Assertions.assertNotNull( result );

        MetadataInfo groupResult = cacheManager.get( groupKey );
        Assertions.assertNotNull( groupResult );

        FileEvent fileEvent = buildFileEvent( new FileEvent( FileEventType.DELETE ) );
        // Use the send method to send a mock message to the events channel. So, our application will process this message.
        emit( fileEvent );

        // Check if the metadata file had been removed from the cache.
        result = cacheManager.get( key );
        Assertions.assertNull( result );

        groupResult = cacheManager.get( groupKey );
        Assertions.assertNull( groupResult );

    }

    private void emit( FileEvent fileEvent )
    {
        Message message = Message.of(fileEvent);
        InMemorySource<Message> events = connector.source( "file-event-in");
        events.send(message);
    }

    private FileEvent buildFileEvent( FileEvent event )
    {
        event.setTargetPath( path );
        event.setStoreKey( storeKey );
        event.setSessionId( "build-0001" );
        return event;
    }

}
