package org.commonjava.service.metadata.cache.infinispan;

import org.commonjava.service.metadata.client.storage.StorageService;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryExpired;
import org.infinispan.notifications.cachelistener.event.CacheEntryExpiredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Listener
@ClientListener
public class MavenMetadataCacheListener
{

    @Inject
    @RestClient
    private StorageService storageService;

    @CacheEntryExpired
    public void metadataExpired( CacheEntryExpiredEvent<MetadataKey, MetadataInfo> event )
    {
        handleMetadataExpired( event.getKey() );
    }

    @ClientCacheEntryExpired
    public void metadataExpired( ClientCacheEntryExpiredEvent<MetadataKey> event )
    {
        handleMetadataExpired( event.getKey() );
    }

    private void handleMetadataExpired( MetadataKey key )
    {
        Logger logger = LoggerFactory.getLogger( getClass() );

        StoreKey storeKey = key.getStoreKey();
        String path = key.getPath();

        try
        {
            storageService.delete( storeKey.toString(), path );
        }
        catch ( Exception e )
        {
            logger.warn( "On cache expiration, metadata file deletion failed for: " + path + " in store: " + storeKey, e );
        }
    }

}
