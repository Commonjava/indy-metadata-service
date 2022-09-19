package org.commonjava.service.metadata.handler;

import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static org.commonjava.service.metadata.handler.MetadataUtil.getMetadataPath;

@ApplicationScoped
public class FileEventConsumer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    MetadataHandler metadataHandler;

    /**
     * this observes the channel of {@link FileEvent} for a pom file, which means maven-metadata.xml will be cleared
     * when a version (pom) is uploaded or removed.
     */
    @Incoming("file-event-in")
    public CompletionStage<Void> receive( Message<FileEvent> message ) {

        // TODO handle the metadata
        Metadata tracingMetadata = message.getMetadata();

        FileEvent event = message.getPayload();

        logger.info("Got an event: {} #{}", event.getEventType().name(), event.getSessionId());

        if ( event.getEventType().equals( FileEventType.STORAGE ) || event.getEventType()
                                                                          .equals( FileEventType.DELETE ) )
        {
            String path = event.getTargetPath();

            if ( !path.endsWith( ".pom" ) )
            {
                return message.ack();
            }

            final String keyStr = event.getStoreKey();
            final String clearPath = getMetadataPath( path );

            logger.info( "Pom file {} {}, will clean matched metadata file {}, store: {}", path, event.getEventType(), clearPath, keyStr );

            final StoreKey key =  StoreKey.fromString( keyStr );

            metadataHandler.doDelete( key, clearPath );

        }
        return message.ack();
    }

}
