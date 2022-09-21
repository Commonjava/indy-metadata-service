package org.commonjava.service.metadata.handler;

import io.opentelemetry.api.trace.Span;
import org.apache.http.HttpStatus;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.commonjava.service.metadata.client.storage.StorageService;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.commonjava.service.metadata.model.StoreType.hosted;

@ApplicationScoped
public class MetadataHandler
{

    @Inject
    @RestClient
    RepositoryService repositoryService;

    @Inject
    @RestClient
    StorageService storageService;

    @Inject
    OtelAdapter otel;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Traced
    public boolean doDelete(StoreKey key, String path )
    {

        Span.current().setAttribute("store.key", key.toString());
        Span.current().setAttribute("path.info", path);

        try
        {
            if ( hosted == key.getType() )
            {
                if ( doClear( key, path ) )
                {
                    logger.info( "Metadata file {} in store {} cleared.", path, key );

                    StoreListingDTO<ArtifactStore> listingDTO = getGroupsAffectdBy( key.toString() );
                    if ( listingDTO != null && listingDTO.items != null )
                    {
                        for ( final ArtifactStore group : listingDTO.items )
                        {
                            if ( doClear( group.key, path ) )
                            {
                                logger.info( "Metadata file {} in store {} cleared.", path, group.key );
                            }
                        }
                        Span.current().setAttribute( "GroupsAffectdBy.size", String.valueOf( listingDTO.items.size()) );
                        logger.info( "Clearing metadata file {} for {} groups affected by {}", path, listingDTO.items.size(), key );
                    }
                }
            }
        }
        catch ( final Exception e )
        {
            logger.warn( "Failed to clear metadata file: {}", path, e );
        }
        return true;

    }

    @Traced
    public StoreListingDTO<ArtifactStore> getGroupsAffectdBy( String key )
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

        try
        {
            if ( key.getType().equals( hosted ) )
            {
                ArtifactStore store = getStore(key);

                if ( store == null )
                {
                    throw new Exception( String.format( "The store doesn't exist, key: %s", key ) );
                }

                if ( isReadonly( store ) )
                {
                    throw new Exception( String.format( "The store %s is readonly. If you want to store any content to this store, please modify it to non-readonly",
                            key ) );
                }
            }

            final boolean isMetadata = path.endsWith(MetadataUtil.METADATA_NAME);

            logger.info("Attempting to delete: {} from {}", path, key);

            boolean result = deleteQuietly( key, path );

            logger.info("Deleted: {} from {} (success? {})", path, key, result);

            if ( result && isMetadata )
            {
                deleteQuietly(key, path + MetadataUtil.MERGEINFO_SUFFIX);
            }

            return result;
        }
        catch ( Exception e )
        {
            logger.warn( "Deletion failed for metadata clear, path: {}, reason: {}", path, e.getMessage() );
        }
        return false;
    }

    private boolean deleteQuietly( final StoreKey key, final String path )
    {
        try
        {
            return deleteResource( key.toString(), path );
        }
        catch ( Exception e )
        {
            logger.warn( "Deletion failed for metadata clear, path: {}, reason: {}", path, e.getMessage() );
        }
        return false;
    }

    @Traced
    public boolean deleteResource( String key, String path )
    {
        Response response = null;

        try
        {
            response = storageService.delete(key, path);
        }
        catch ( WebApplicationException e )
        {
            throw e;
        }
        if ( Response.Status.fromStatusCode( response.getStatus() ).getFamily()
                != Response.Status.Family.SUCCESSFUL  )
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    public boolean isReadonly( final ArtifactStore store )
    {
        return store != null && store.key.getType() == hosted && store.readonly;
    }
    @Traced
    public ArtifactStore getStore( StoreKey key )
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
