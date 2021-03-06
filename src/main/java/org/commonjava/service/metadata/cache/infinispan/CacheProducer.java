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
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.impl.ResourceUtils;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class CacheProducer
{
    Logger logger = LoggerFactory.getLogger( getClass() );

    private RemoteCacheManager remoteCacheManager;

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
    }

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

                    logger.debug( "Cache config: {}", confStr );

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

        return handle;
    }

    public synchronized void registerProtoAndMarshallers( String protofile, List<BaseMarshaller> marshallers )
    {
        SerializationContext ctx = MarshallerUtil.getSerializationContext( remoteCacheManager );
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
                        remoteCacheManager.getCache( ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME );

        logger.info( "Registering proto files: {}", protofile );

        metadataCache.put( protofile, ResourceUtils.getResourceAsString( getClass(), "/" + protofile ));

    }

    public synchronized void stop()
    {
        logger.info( "Stopping Infinispan caches." );
        caches.forEach( ( name, cacheHandle ) -> cacheHandle.stop() );

        if ( remoteCacheManager != null )
        {
            remoteCacheManager.stop();
            remoteCacheManager = null;
        }

    }

    private String interpolateStrFromStream( InputStream inputStream, String path )
    {
        String configuration;
        try
        {
            configuration = IOUtils.toString( inputStream, Charset.defaultCharset() );
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

}
