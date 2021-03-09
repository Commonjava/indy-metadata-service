package org.commonjava.service.metadata.handler;

import io.quarkus.test.Mock;
import org.commonjava.service.metadata.cache.infinispan.BasicCacheHandle;
import org.commonjava.service.metadata.cache.infinispan.CacheHandle;
import org.commonjava.service.metadata.cache.infinispan.CacheProducer;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@Mock
@ApplicationScoped
public class MockableCacheProducer
                extends CacheProducer
{

    DefaultCacheManager cacheManager;

    @PostConstruct
    private void init() throws Exception
    {
        // Use DefaultCacheManager for unit test
        cacheManager = new DefaultCacheManager( "infinispan.xml" );
    }

    public synchronized <K, V> BasicCacheHandle<K, V> getBasicCache( String named )
    {
        Cache<K, V> cache = cacheManager.getCache( named );
        return new CacheHandle( named, cache );
    }

}
