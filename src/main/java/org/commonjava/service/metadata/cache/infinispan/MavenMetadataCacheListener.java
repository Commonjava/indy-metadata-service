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
