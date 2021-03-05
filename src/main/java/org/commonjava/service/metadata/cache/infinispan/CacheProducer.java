/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.commonjava.service.metadata.config.ISPNConfiguration;
import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.counter.api.StrongCounter;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Externalizable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

@ApplicationScoped
public class CacheProducer
{
    Logger logger = LoggerFactory.getLogger( getClass() );

    private EmbeddedCacheManager cacheManager;

    @Inject
    private ISPNConfiguration ispnConfiguration;

    private Map<String, BasicCacheHandle> caches = new ConcurrentHashMap<>(); // hold embedded and remote caches

    protected CacheProducer()
    {
    }

    @PostConstruct
    public void start()
    {
        startRemoteManager();
        //startEmbeddedManager();
    }

    private RemoteCacheManager remoteCacheManager;

    private void startRemoteManager()
    {
        if ( ispnConfiguration == null || !ispnConfiguration.isEnabled() )
        {
            logger.info( "Infinispan remote configuration not enabled. Skip." );
            return;
        }

        ConfigurationBuilder builder = new ConfigurationBuilder();
        Properties props = new Properties();
        try( Reader config = new FileReader( ispnConfiguration.getHotrodClientConfigPath()) )
        {
            props.load( config );
            builder.withProperties( props );
        }
        catch ( IOException e )
        {
            logger.error( "Load hotrod client properties failure.", e );
        }

        remoteCacheManager = new RemoteCacheManager(builder.build());
        remoteCacheManager.start();

        logger.info( "Infinispan remote cache manager started." );
    }

    /**
     * Get a BasicCache instance. If the remote cache is enabled, it will match the named with remote.patterns.
     * If matched, it will create/return a RemoteCache. If not matched, an embedded cache will be created/returned to the caller.
     */
    public synchronized <K, V> BasicCacheHandle<K, V> getBasicCache( String named )
    {
        BasicCacheHandle handle = caches.computeIfAbsent( named, ( k ) -> {
            if ( ispnConfiguration.isEnabled() )
            {
                RemoteCache<K, V> cache = null;
                try
                {
                    // For infinispan 9.x, it needs to load the specific cache configuration to create it
                    // For infinispan 11.x, there is no need to load this configuration here, instead, declaring it
                    // in hotrod-client.properties and get the cache by remoteCacheManager.getCache( "cacheName" )
                    File confDir = ispnConfiguration.getCacheConfigDir();
                    File cacheConf = new File( confDir, "caches/cache-" + named + ".xml" );
                    if ( !cacheConf.exists() )
                    {
                        logger.warn( "Invalid conf path, name: {}, path: {}", named, cacheConf );
                        return null;
                    }
                    String confStr;
                    try (InputStream confStream = FileUtils.openInputStream( cacheConf ))
                    {
                        confStr = interpolateStrFromStream( confStream, cacheConf.getPath() );
                    }
                    catch ( IOException e )
                    {
                        throw new RuntimeException( "Cannot read cache configuration from file: " + cacheConf, e );
                    }
                    cache = remoteCacheManager.administration().getOrCreateCache( named, new XMLStringConfiguration( confStr ) );
                    if ( cache == null )
                    {
                        logger.warn( "Can not get remote cache, name: {}", k );
                        return null;
                    }
                }
                catch ( Exception e )
                {
                    logger.warn( "Get remote cache failed", e );
                    return null;
                }
                logger.info( "Get remote cache, name: {}", k );
                return new RemoteCacheHandle( k, cache );
            }
            return null;
        } );

        if ( handle == null )
        {
            handle = getCache( named );
        }

        return handle;
    }

    public synchronized void registerProtoAndMarshallers( String protofile, List<BaseMarshaller> marshallers )
    {
        SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext( remoteCacheManager );
        try
        {
            ctx.registerProtoFiles( FileDescriptorSource.fromResources( protofile ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException("Register proto files error, protofile: " + protofile, e);
        }

        for ( BaseMarshaller marshaller : marshallers )
        {
            try
            {
                ctx.registerMarshaller( marshaller );
            }
            catch ( Exception e )
            {
                throw new RuntimeException("Register the marshallers error.", e);
            }
        }

        // Retrieve metadata cache and register the new schema on the infinispan server too
        RemoteCache<String, String> metadataCache =
                        remoteCacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

        metadataCache.put( protofile, FileDescriptorSource.getResourceAsString( getClass(), "/" + protofile ));

    }

    /**
     * Get named cache and verify that the cache obeys our expectations for clustering.
     * There is no way to find out the runtime type of generic type parameters and we need to pass the k/v class types.
     */
    public synchronized <K, V> CacheHandle<K, V> getClusterizableCache( String named, Class<K> kClass, Class<V> vClass )
    {
        verifyClusterizable( kClass, vClass );
        return getCache( named );
    }

    private <K, V> void verifyClusterizable( Class<K> kClass, Class<V> vClass )
    {
        if ( !Serializable.class.isAssignableFrom( kClass ) && !Externalizable.class.isAssignableFrom( kClass )
                        || !Serializable.class.isAssignableFrom( vClass ) && !Externalizable.class.isAssignableFrom(
                        vClass ) )
        {
            throw new RuntimeException( kClass + " or " + vClass + " is not Serializable/Externalizable" );
        }
    }

    /**
     * Retrieve an embedded cache with a pre-defined configuration (from infinispan.xml) or the default cache configuration.
     */
    public synchronized <K, V> CacheHandle<K, V> getCache( String named )
    {
        logger.debug( "Get embedded cache, name: {}", named );
        return (CacheHandle) caches.computeIfAbsent( named, ( k ) -> {
            Cache<K, V> cache = cacheManager.getCache( k );

            return new CacheHandle( k, cache );
        } );
    }

    public synchronized Configuration getCacheConfiguration( String name )
    {
        if ( cacheManager == null )
        {
            throw new IllegalStateException( "Cannot access CacheManager. Indy seems to be in a state of shutdown." );
        }
        return cacheManager.getCacheConfiguration( name );
    }

    public synchronized Configuration getDefaultCacheConfiguration()
    {
        if ( cacheManager == null )
        {
            throw new IllegalStateException( "Cannot access CacheManager. Indy seems to be in a state of shutdown." );
        }

        return cacheManager.getDefaultCacheConfiguration();
    }

    public synchronized Configuration setCacheConfiguration( String name, Configuration config )
    {
        if ( cacheManager == null )
        {
            throw new IllegalStateException( "Cannot access CacheManager. Indy seems to be in a state of shutdown." );
        }
        return cacheManager.defineConfiguration( name, config );
    }

    public synchronized void stop()
    {
        logger.info( "Stopping Infinispan caches." );
        caches.forEach( ( name, cacheHandle ) -> cacheHandle.stop() );

        if ( cacheManager != null )
        {
            cacheManager.stop();
            cacheManager = null;
        }

        if ( remoteCacheManager != null )
        {
            remoteCacheManager.stop();
            remoteCacheManager = null;
        }

    }

    public int getShutdownPriority()
    {
        return 10;
    }

    public String getId()
    {
        return "infinispan-caches";
    }

    private String interpolateStrFromStream( InputStream inputStream, String path )
    {
        String configuration;
        try
        {
            configuration = IOUtils.toString( inputStream );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Cannot read infinispan configuration from : " + path, e );
        }

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource( new PropertiesBasedValueSource( System.getProperties() ) );

        try
        {
            configuration = interpolator.interpolate( configuration );
        }
        catch ( InterpolationException e )
        {
            throw new RuntimeException( "Cannot resolve expressions in infinispan configuration from: " + path, e );
        }
        return configuration;
    }

    public EmbeddedCacheManager getCacheManager()
    {
        return cacheManager;
    }

}
