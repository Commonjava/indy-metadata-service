package org.commonjava.service.metadata.handler;

import org.commonjava.event.promote.PathsPromoteCompleteEvent;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
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
public class PromoteEventConsumer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String POM_EXTENSION = ".pom";

    private static final String PACKAGE_TARBALL_EXTENSION = ".tgz";

    @Inject
    MetadataHandler metadataHandler;

    @Incoming("promote-event-in")
    public CompletionStage<Void> receive( Message<PathsPromoteCompleteEvent> message )
    {

        PathsPromoteCompleteEvent event = message.getPayload();

        logger.info( "Got an event: {}", event );

        final String keyStr = event.getTargetStore();
        final StoreKey key =  StoreKey.fromString( keyStr );

        StoreListingDTO<ArtifactStore> groupsAffectedBy = metadataHandler.getGroupsAffectdBy(key.toString());

        event.getCompletedPaths().forEach( path ->
        {
            if ( hosted != key.getType() )
            {
                return;
            }

            String clearPath = null;

            if ( path.endsWith( POM_EXTENSION ) )
            {
                clearPath = getMetadataPath( path );

                logger.info( "Pom file {}, will clean matched metadata file {}, store: {}", path, clearPath, keyStr );

            }
            else if ( path.endsWith( PACKAGE_TARBALL_EXTENSION ) )
            {
                clearPath = getPkgMetadataPath( path );

                logger.info( "Tar file {}, will clean matched metadata file {}, store: {}", path, clearPath, keyStr );
            }

            if ( hosted == key.getType() )
            {
                metadataHandler.doDelete(key, groupsAffectedBy, clearPath);
            }
        } );

        return message.ack();

    }

}
