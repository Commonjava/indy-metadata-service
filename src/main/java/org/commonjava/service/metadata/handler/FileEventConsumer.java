package org.commonjava.service.metadata.handler;

import org.apache.http.HttpStatus;
import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;
import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.commonjava.service.metadata.client.storage.StorageService;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

import static org.commonjava.service.metadata.handler.MetadataUtil.getMetadataPath;
import static org.commonjava.service.metadata.model.StoreType.hosted;

@ApplicationScoped
public class FileEventConsumer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    @RestClient
    StorageService storageService;

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
            try
            {
                if ( hosted == key.getType() )
                {
                    if ( doClear( key, clearPath ) )
                    {
                        logger.info( "Metadata file {} in store {} cleared.", clearPath, key );

                        StoreListingDTO<ArtifactStore> listingDTO = getGroupsAffectdBy( key.toString() );
                        if ( listingDTO != null && listingDTO.items != null )
                        {
                            for ( final ArtifactStore group : listingDTO.items )
                            {
                                if ( doClear( group.key, clearPath ) )
                                {
                                    logger.info( "Metadata file {} in store {} cleared.", clearPath, group.key );
                                }
                            }
                            logger.info( "Clearing metadata file {} for {} groups affected by {}", clearPath, listingDTO.items.size(), key );
                        }
                    }
                }
            }
            catch ( final Exception e )
            {
                logger.warn( "Failed to clear maven-metadata.xml: {}", path, e );
            }

        }
        return message.ack();
    }

    private StoreListingDTO<ArtifactStore> getGroupsAffectdBy( String key )
    {
        Response response;

        try
        {
            response = repositoryService.getGroupsAffectedBy( key );
        }
        catch ( WebApplicationException e )
        {
            if (e.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND )
            {
                return null;
            }
            else
            {
                throw e;
            }
        }
        if ( response != null && response.getStatus() == HttpStatus.SC_OK )
        {
            return response.readEntity(StoreListingDTO.class);
        }

        return null;

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
            ArtifactStore store = getStore( key );

            if ( store == null )
            {
                throw new Exception(String.format("The store doesn't exist, key: %s", key));
            }

            if ( store.readonly )
            {
                throw new Exception(String.format("The store %s is readonly. If you want to store any content to this store, please modify it to non-readonly",
                        key));
            }
            storageService.delete( key.toString(), path );
            return true;
        }
        catch ( Exception e )
        {
            logger.warn( "Deletion failed for metadata clear, path: {}, reason: {}", path, e.getMessage() );
        }
        return false;
    }

    public boolean isReadonly( final ArtifactStore store )
    {
        return store != null && store.key.getType() == hosted && store.readonly;
    }

    private ArtifactStore getStore( StoreKey key )
    {

        Response response;

        try
        {
            response = repositoryService.getStore(key.getPackageType(), key.getType().name(), key.getName());
        }
        catch ( WebApplicationException e )
        {
            if (e.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND )
            {
                return null;
            }
            else
            {
                throw e;
            }
        }
        if ( response != null && response.getStatus() == HttpStatus.SC_OK )
        {
            return response.readEntity(ArtifactStore.class);
        }

        return null;

    }

}
