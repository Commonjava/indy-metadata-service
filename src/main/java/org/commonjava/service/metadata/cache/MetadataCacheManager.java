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
package org.commonjava.service.metadata.cache;

import org.commonjava.service.metadata.cache.infinispan.BasicCacheHandle;
import org.commonjava.service.metadata.cache.infinispan.CacheHandle;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class MetadataCacheManager
{
    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    @MavenMetadataCache
    private BasicCacheHandle<MetadataKey, MetadataInfo> metadataCache;

    @Inject
    @MavenMetadataKeyCache
    private BasicCacheHandle<MetadataKey, MetadataKey> metadataKeyCache;

    private QueryFactory queryFactory;

    public MetadataCacheManager()
    {
    }

    @PostConstruct
    private void init()
    {
        if ( metadataKeyCache.getCache() instanceof RemoteCache )
        {
            this.queryFactory = org.infinispan.client.hotrod.Search.getQueryFactory(
                            (RemoteCache) metadataKeyCache.getCache() );
        }
        else
        {
            this.queryFactory = org.infinispan.query.Search.getQueryFactory( (Cache) metadataKeyCache.getCache() );
        }
    }

    public MetadataCacheManager( CacheHandle<MetadataKey, MetadataInfo> metadataCache,
                                 CacheHandle<MetadataKey, MetadataKey> metadataKeyCache )
    {
        this.metadataCache = metadataCache;
        this.metadataKeyCache = metadataKeyCache;
    }

    public void put( MetadataKey metadataKey, MetadataInfo metadataInfo )
    {
        metadataKeyCache.put( metadataKey, metadataKey );
        metadataCache.put( metadataKey, metadataInfo );
    }

    public MetadataInfo get( MetadataKey metadataKey )
    {
        return metadataCache.get( metadataKey );
    }

    public void remove( StoreKey storeKey, String path )
    {
        remove( new MetadataKey( storeKey, path ) );
    }

    public void remove( StoreKey key, Set<String> paths )
    {
        paths.forEach( p -> remove( new MetadataKey( key, p ) ) );
    }

    public void remove( MetadataKey metadataKey )
    {
        metadataKeyCache.remove( metadataKey );
        metadataCache.remove( metadataKey );
    }

    public void removeAll( StoreKey key )
    {
        getMatches( key ).forEach( k -> remove( k ) );
    }

    public Set<String> getAllPaths( StoreKey key )
    {
        return getMatches( key ).stream().map( ( k ) -> k.getPath() ).collect( Collectors.toSet() );
    }

    private List<MetadataKey> getMatches( StoreKey key )
    {

        String entity = ( metadataKeyCache.getCache() instanceof RemoteCache ) ?
                        "metadata_key.MetadataKey" :
                        "org.commonjava.service.metadata.model.MetadataKey";

        Query query = queryFactory.create( String.format( "FROM %s k WHERE k.storeKey.packageType=:packageType "
                                                                          + "AND k.storeKey.type=:type "
                                                                          + "AND k.storeKey.name=:name",
                                                          entity ) )
                                  .setParameter( "packageType", key.getPackageType() )
                                  .setParameter( "type", key.getType() )
                                  .setParameter( "name", key.getName() );

        List<MetadataKey> matches = query.execute().list();

        logger.debug( "Query metadataKeyCache for storeKey: {}, size: {}", key, matches.size() );
        return matches;
    }

}
