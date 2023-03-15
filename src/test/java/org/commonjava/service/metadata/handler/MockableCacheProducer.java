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
