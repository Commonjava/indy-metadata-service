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

import org.commonjava.service.metadata.cache.MavenMetadataCache;
import org.commonjava.service.metadata.cache.MavenMetadataKeyCache;
import org.commonjava.service.metadata.cache.infinispan.marshaller.MetadataInfoMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.MetadataKeyMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.MetadataMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.SnapshotMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.SnapshotVersionMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.StoreKeyMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.StoreTypeMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.VersioningMarshaller;
import org.commonjava.service.metadata.config.ISPNConfiguration;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.protostream.BaseMarshaller;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MetadataCacheProducer
{
    private static final String METADATA_KEY_CACHE = "maven-metadata-key-cache";

    private static final String METADATA_CACHE = "maven-metadata-cache";

    @Inject
    private MavenMetadataCacheListener cacheListener;

    @Inject
    private CacheProducer cacheProducer;

    @Inject
    private ISPNConfiguration ispnConfiguration;

    @MavenMetadataCache
    @Produces
    @ApplicationScoped
    public BasicCacheHandle<MetadataKey, MetadataInfo> mavenMetadataCacheCfg()
    {
        if ( ispnConfiguration.isEnabled() )
        {
            List<BaseMarshaller> infoMarshallers = new ArrayList<>();
            infoMarshallers.add( new MetadataInfoMarshaller() );
            infoMarshallers.add( new MetadataMarshaller() );
            infoMarshallers.add( new VersioningMarshaller() );
            infoMarshallers.add( new SnapshotMarshaller() );
            infoMarshallers.add( new SnapshotVersionMarshaller() );
            infoMarshallers.add( new VersioningMarshaller() );
            cacheProducer.registerProtoAndMarshallers( "metadata_info.proto", infoMarshallers );

            List<BaseMarshaller> keyMarshallers = new ArrayList<>();
            keyMarshallers.add( new MetadataKeyMarshaller() );
            keyMarshallers.add( new StoreKeyMarshaller() );
            keyMarshallers.add( new StoreTypeMarshaller() );
            cacheProducer.registerProtoAndMarshallers( "metadata_key.proto", keyMarshallers );
        }
        return cacheProducer.getBasicCache( METADATA_CACHE );
    }

    @MavenMetadataKeyCache
    @Produces
    @ApplicationScoped
    public BasicCacheHandle<MetadataKey, MetadataKey> mavenMetadataKeyCacheCfg()
    {
        if ( ispnConfiguration.isEnabled() )
        {
            List<BaseMarshaller> keyMarshallers = new ArrayList<>();
            keyMarshallers.add( new MetadataKeyMarshaller() );
            keyMarshallers.add( new StoreKeyMarshaller() );
            keyMarshallers.add( new StoreTypeMarshaller() );
            cacheProducer.registerProtoAndMarshallers( "metadata_key.proto", keyMarshallers );
        }

        BasicCacheHandle<MetadataKey, MetadataKey> handler = cacheProducer.getBasicCache( METADATA_KEY_CACHE );

        addListener( handler );

        return handler;
    }

    private void addListener( BasicCacheHandle handler )
    {
        if ( handler.getCache() instanceof RemoteCache )
        {
            ((RemoteCache)handler.getCache()).addClientListener( cacheListener );
        }
        else
        {
            ((Cache)handler.getCache()).addListener( cacheListener );
        }
    }

}
