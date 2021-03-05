package org.commonjava.service.metadata.cache;

import org.commonjava.service.metadata.cache.infinispan.BasicCacheHandle;
import org.commonjava.service.metadata.cache.infinispan.CacheHandle;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
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
        /*else
        {
            this.queryFactory = Search.getQueryFactory( (Cache) metadataKeyCache.getCache() );
        }*/
    }

    public MetadataCacheManager( CacheHandle<MetadataKey, MetadataInfo> metadataCache,
                                 CacheHandle<MetadataKey, MetadataKey> metadataKeyCache )
    {
        this.metadataCache = metadataCache;
        this.metadataKeyCache = metadataKeyCache;
        //this.queryFactory = Search.getQueryFactory( metadataKeyCache.getCache() );
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
        Query query = queryFactory.from( MetadataKey.class )
                                  .having( "storeKey.packageType" )
                                  .eq( key.getPackageType() )
                                  .and()
                                  .having( "storeKey.type" )
                                  .eq( key.getType().toString() )
                                  .and()
                                  .having( "storeKey.name" )
                                  .eq( key.getName() )
                                  .build();
        List<MetadataKey> matches = query.list();

        logger.debug( "Query metadataKeyCache for storeKey: {}, size: {}", key, matches.size() );
        return matches;
    }

}
