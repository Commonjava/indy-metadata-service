package org.commonjava.service.metadata.handler;

import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;
import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.client.pathmapped.PathmappedService;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.commonjava.service.metadata.handler.MetadataUtil.getMetadataPath;
import static org.commonjava.service.metadata.model.StoreType.hosted;

@ApplicationScoped
public class FileEventConsumer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    @RestClient
    PathmappedService pathmappedService;

    @Inject
    @RestClient
    RepositoryService repositoryService;

    @Inject
    MetadataCacheManager cacheManager;

    /**
     * this observes the channel of {@link FileEvent} for a pom file, which means maven-metadata.xml will be cleared
     * when a version (pom) is uploaded or removed.
     */
    @Incoming("file-event-in")
    public void receive( FileEvent event ) {

        logger.info("Got an event: {} #{}", event.getEventType().name(), event.getSessionId());

        if ( event.getEventType().equals( FileEventType.STORAGE ) || event.getEventType()
                                                                          .equals( FileEventType.DELETE ) )
        {
            String path = event.getTargetPath();

            if ( !path.endsWith( ".pom" ) )
            {
                return;
            }

            final String keyStr = event.getStoreKey();
            final String clearPath = getMetadataPath( path );

            logger.info( "Pom file {} {}, will clean matched metadata file {}, store: {}", path, event.getEventType(), clearPath, keyStr );

            final StoreKey key =  StoreKey.fromString( keyStr );
            try
            {
                if ( hosted == key.getType() )
                {
                    if ( doClear( key, clearPath ) )
                    {
                        cacheManager.remove( key, clearPath );
                        logger.info( "Metadata file {} in store {} cleared.", clearPath, key );
                    }

                    StoreListingDTO<ArtifactStore> listingDTO = repositoryService.getGroupsAffectedBy( key.toString() );
                    for ( final ArtifactStore group : listingDTO.items )
                    {
                        cacheManager.remove( group.key, clearPath );
                    }
                }
            }
            catch ( final Exception e )
            {
                logger.warn( "Failed to clear maven-metadata.xml: {}", path, e );
            }

        }

    }

    private boolean doClear( final StoreKey key, final String path )
    {
        logger.trace( "Updating merged metadata file: {} in store: {}", path, key );

        final boolean isMetadata = path.endsWith( MetadataUtil.METADATA_NAME );

        logger.trace( "Attempting to delete: {}", path );

        boolean result = deleteQuietly( key, path );
        logger.trace( "Deleted: {} (success? {})", path, result );
        if ( result && isMetadata )
        {
            deleteQuietly( key, path + MetadataUtil.MERGEINFO_SUFFIX );
        }
        return result;
    }

    private boolean deleteQuietly( final StoreKey key, final String path )
    {
        try
        {
            pathmappedService.delete( key.getPackageType(), key.getType().name(), key.getName(), path );
            return true;
        }
        catch ( Exception e )
        {
            logger.warn( "Deletion failed for metadata clear, path: {}, reason: {}", path, e.getMessage() );
        }
        return false;
    }

}
