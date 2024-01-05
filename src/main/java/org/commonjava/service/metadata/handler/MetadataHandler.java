/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.metadata.handler;

import io.opentelemetry.api.trace.Span;
import org.apache.http.HttpStatus;
import org.commonjava.event.storage.BatchCleanupRequest;
import org.commonjava.event.storage.BatchCleanupResult;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.commonjava.service.metadata.client.storage.StorageService;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Set;

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

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Traced
    public boolean doDelete(StoreKey key, String path )
    {
        StoreListingDTO<ArtifactStore> groupsAffectedBy = getGroupsAffectdBy(key.toString());
        return doDelete(key, groupsAffectedBy, path );
    }

    @Traced
    public boolean doDelete(StoreKey key, StoreListingDTO<ArtifactStore> groupsAffectedBy, String path )
    {
        Span.current().setAttribute("store.key", key.toString());
        Span.current().setAttribute("path.info", path);
        try
        {
            if ( doClear( key, path ) )
            {
                logger.info( "Metadata file {} in store {} cleared.", path, key );

                if (groupsAffectedBy != null && groupsAffectedBy.items != null)
                {
                    for (final ArtifactStore group : groupsAffectedBy.items)
                    {
                        if ( doClear( group.key, path ) )
                        {
                            logger.info( "Metadata file {} in store {} cleared.", path, group.key );
                        }
                    }
                    Span.current().setAttribute( "GroupsAffectedBy.size", groupsAffectedBy.items.size() );
                    logger.info("Clearing metadata file {} for {} groups affected by {}", path, groupsAffectedBy.items.size(), key);
                }

            }
        }
        catch ( final Exception e )
        {
            logger.warn( "Failed to clear metadata file: {}, error: {}", path, e );
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
                    throw new IndyException( String.format( "The store doesn't exist, key: %s", key ) );
                }

                if ( isReadonly( store ) )
                {
                    throw new IndyException( String.format( "The store %s is readonly. If you want to store any content to this store, please modify it to non-readonly",
                            key ) );
                }
            }

            final boolean isMetadata = path.endsWith(MetadataUtil.METADATA_NAME) || path.endsWith(MetadataUtil.NPM_METADATA_NAME);

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
    public boolean deleteResource( String key, String path ) throws IndyException {
        Response response = null;

        try
        {
            response = storageService.delete(key, path);
        }
        catch ( WebApplicationException e )
        {
            throw new IndyException("Delete resource error.", e);
        }
        if ( Response.Status.fromStatusCode( response.getStatus() ).getFamily()
                != Response.Status.Family.SUCCESSFUL  )
        {
            throw new IndyException("Delete resource error with status code {}", response.getStatus());
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

    public void doBatchDelete( Set<String> clearPaths, Set<String> filesystems )
    {
        Response response = null;

        try
        {
            BatchCleanupRequest request = new BatchCleanupRequest();
            request.setFilesystems(filesystems);
            request.setPaths(clearPaths);

            logger.info( "Batch delete request, {}", request );
            response = storageService.cleanup( request );
        }
        catch ( WebApplicationException e )
        {
            logger.warn( "Delete resource error: {}", e );
        }

        if ( response != null && response.getStatus() == HttpStatus.SC_OK )
        {
            if ( Response.Status.fromStatusCode( response.getStatus() ).getFamily()
                    != Response.Status.Family.SUCCESSFUL  )
            {
                logger.warn( "Delete resource error with status code {}", response.getStatus() );
            }

            if ( response.getStatus() == HttpStatus.SC_OK )
            {
                BatchCleanupResult result = response.readEntity(BatchCleanupResult.class);
                logger.info( "Batch delete result, {}", result );
            }
        }

    }
}
