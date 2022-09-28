package org.commonjava.service.metadata.handler;

import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static org.commonjava.service.metadata.handler.MetadataUtil.getMetadataPath;
import static org.commonjava.service.metadata.handler.MetadataUtil.getPkgMetadataPath;
import static org.commonjava.service.metadata.model.StoreType.hosted;

@ApplicationScoped
public class FileEventConsumer
{

    private static final String POM_EXTENSION = ".pom";

    private static final String PACKAGE_TARBALL_EXTENSION = ".tgz";
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    MetadataHandler metadataHandler;

    /**
     * this observes the channel of {@link FileEvent} for a pom file, which means maven-metadata.xml will be cleared
     * when a version (pom) is uploaded or removed.
     */
    @Incoming("file-event-in")
    public CompletionStage<Void> receive( Message<FileEvent> message ) {

        FileEvent event = message.getPayload();

        logger.info("Got an event: {} {}:{}", event.getEventType().name(), event.getStoreKey(), event.getTargetPath());

        if ( event.getEventType().equals( FileEventType.STORAGE ) || event.getEventType()
                                                                          .equals( FileEventType.DELETE ) )
        {
            String path = event.getTargetPath();

            if ( !path.endsWith( POM_EXTENSION ) && !path.endsWith( PACKAGE_TARBALL_EXTENSION ) )
            {
                return message.ack();
            }

            final String keyStr = event.getStoreKey();
            final StoreKey key =  StoreKey.fromString( keyStr );

            if ( hosted != key.getType() )
            {
                return message.ack();
            }

            String clearPath = null;

            if ( path.endsWith( POM_EXTENSION ) )
            {
                clearPath = getMetadataPath( path );

                logger.info( "Pom file {} {}, will clean matched metadata file {}, store: {}", path, event.getEventType(), clearPath, keyStr );

            }
            else if ( path.endsWith( PACKAGE_TARBALL_EXTENSION ) )
            {
                clearPath = getPkgMetadataPath( path );

                logger.info( "Tar file {} {}, will clean matched metadata file {}, store: {}", path, event.getEventType(), clearPath, keyStr );
            }

            if ( hosted == key.getType() )
            {
                metadataHandler.doDelete(key, clearPath);
            }

        }
        return message.ack();
    }

}
