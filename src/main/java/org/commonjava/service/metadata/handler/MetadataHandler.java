package org.commonjava.service.metadata.handler;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
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

    public boolean doDelete( StoreKey key, String path )
    {

        Scope scope = null;
        Span span = otel.newClientSpan("Metadata Handler", "metadata_cleanup");

        try
        {
            if ( span != null )
            {
                scope = span.makeCurrent();
            }
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
                        addAttributeToSpan( span, "GroupsAffectdBy.size", String.valueOf( listingDTO.items.size()) );
                        logger.info( "Clearing metadata file {} for {} groups affected by {}", path, listingDTO.items.size(), key );
                    }
                }
            }
        }
        catch ( final Exception e )
        {
            logger.warn( "Failed to clear metadata file: {}", path, e );
        }
        finally
        {
            scope.close();
            span.end();
        }
        return true;

    }

    private void addAttributeToSpan(Span span, String key, String value)
    {
        if ( span != null )
        {
            span.setAttribute( key, value );
        }
    }

    private StoreListingDTO<ArtifactStore> getGroupsAffectdBy( String key )
    {
        Response response;
        Scope scope = null;

        Span span = otel.newClientSpan("REST Client", "remote.call:repository.service");

        try
        {
            if ( span != null )
            {
                scope = span.makeCurrent();
            }
            addAttributeToSpan( span, "store.key", key );
            response = repositoryService.getGroupsAffectedBy( key );
        }
        catch ( WebApplicationException e )
        {
            if ( span != null )
            {
                span.recordException(e);
                scope.close();
                span.end();
            }
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
            if ( span != null )
            {
                span.setStatus(StatusCode.OK);
                scope.close();
                span.end();
            }
            return response.readEntity(StoreListingDTO.class);
        }

        return null;

    }

    private boolean doClear( final StoreKey key, final String path )
    {
        logger.trace( "Updating merged metadata file: {} in store: {}", path, key );

        try
        {
            ArtifactStore store = getStore(key);

            if (store == null)
            {
                throw new Exception(String.format("The store doesn't exist, key: %s", key));
            }

            if (isReadonly(store))
            {
                throw new Exception(String.format("The store %s is readonly. If you want to store any content to this store, please modify it to non-readonly",
                        key));
            }

            final boolean isMetadata = path.endsWith(MetadataUtil.METADATA_NAME);

            logger.info("Attempting to delete: {} from {}", path, key);

            boolean result = deleteQuietly(key, path);

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
            return doDelete( key.toString(), path );
        }
        catch ( Exception e )
        {
            logger.warn( "Deletion failed for metadata clear, path: {}, reason: {}", path, e.getMessage() );
        }
        return false;
    }

    private boolean doDelete(String key, String path)
    {
        Span span = null;
        Scope scope = null;

        if ( otel.enabled() )
        {
            span = otel.newClientSpan("REST Client", "remote.call:storage.service");
        }
        try
        {
            if ( span != null )
            {
                scope = span.makeCurrent();
            }
            addAttributeToSpan( span, "store.key", key );
            addAttributeToSpan( span, "path.info", path );

            storageService.delete(key, path);
        }
        catch ( WebApplicationException e )
        {
            if ( span != null )
            {
                span.recordException( e );
                scope.close();
                span.end();
            }
            {
                throw e;
            }
        }
        if ( span != null )
        {
            span.setStatus( StatusCode.OK );
            scope.close();
            span.end();
        }
        return true;

    }

    public boolean isReadonly( final ArtifactStore store )
    {
        return store != null && store.key.getType() == hosted && store.readonly;
    }

    private ArtifactStore getStore( StoreKey key )
    {

        Response response;
        Span span = null;
        Scope scope = null;

        if ( otel.enabled() )
        {
            span = otel.newClientSpan("REST Client", "remote.call:repository.service");
        }

        try
        {
            if ( span != null )
            {
                scope = span.makeCurrent();
                span.setAttribute("repository.info", key.toString());
            }
            response = repositoryService.getStore(key.getPackageType(), key.getType().name(), key.getName());
        }
        catch ( WebApplicationException e )
        {
            if ( span != null )
            {
                span.recordException(e);
                scope.close();
                span.end();
            }
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
            if ( span != null )
            {
                span.setStatus(StatusCode.OK);
                scope.close();
                span.end();
            }
            return response.readEntity(ArtifactStore.class);
        }
        return null;

    }
}
