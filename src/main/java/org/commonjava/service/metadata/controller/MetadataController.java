package org.commonjava.service.metadata.controller;

import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.handler.MetadataHandler;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.commonjava.service.metadata.model.StoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class MetadataController
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    MetadataCacheManager cacheManager;

    @Inject
    MetadataHandler metadataHandler;

    public MetadataInfo getMetadataInfo( String packageType, String type, String name, String path )
    {

        String storeKeyStr = packageType + ":" + type + ":" + name;
        MetadataKey key = new MetadataKey( StoreKey.fromString( storeKeyStr ), path );

        return cacheManager.get( key );
    }

    public Set<String> getAllPaths( String packageType, String type, String name )
    {

        String storeKeyStr = packageType + ":" + type + ":" + name;
        return cacheManager.getAllPaths( StoreKey.fromString( storeKeyStr ) );
    }

    public boolean doDelete(String packageType, String type, String name, String path)
    {
        return metadataHandler.doDelete( new StoreKey(packageType, StoreType.get(type), name), path );
    }
}
