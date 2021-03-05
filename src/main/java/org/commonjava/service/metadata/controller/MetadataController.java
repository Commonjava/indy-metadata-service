package org.commonjava.service.metadata.controller;

import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MetadataController
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    MetadataCacheManager cacheManager;

    public MetadataInfo getMetadataInfo( String packageType, String type, String name, String path )
    {

        String storeKeyStr = packageType + ":" + type + ":" + name;
        MetadataKey key = new MetadataKey( StoreKey.fromString( storeKeyStr ), path );

        return cacheManager.get( key );
    }
}
