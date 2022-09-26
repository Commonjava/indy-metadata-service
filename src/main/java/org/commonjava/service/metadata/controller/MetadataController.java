package org.commonjava.service.metadata.controller;

import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.handler.MetadataHandler;
import org.commonjava.service.metadata.handler.SpecialPathManager;
import org.commonjava.service.metadata.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@ApplicationScoped
public class MetadataController
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    MetadataCacheManager cacheManager;

    @Inject
    MetadataHandler metadataHandler;

    @Inject
    SpecialPathManager specialPathManager;

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

    public SpecialPathDTO getSpecialPathSets( String packageType )
    {
        SpecialPathDTO specialPathDTO = new SpecialPathDTO();
        if ( "all".equals(packageType) )
        {
            specialPathDTO.setStdSpecialPaths(specialPathManager.getRegisteredSpecialInfo());
            specialPathDTO.setSpecialPathSets(specialPathManager.getRegisteredSpecialPathSet());
        }
        else
        {
            specialPathDTO.setSpecialPathSets(Collections.singleton(specialPathManager.getRegisteredSpecialPathSet(packageType)));
        }
        return specialPathDTO;
    }

    public SpecialPathInfo getSpecialPathInfo(String packageType, String type, String name, String path)
    {
        logger.info("getSpecialPathInfo: {}:{}:{}/{} ", packageType, type, name, path);
        SpecialPathInfo info = specialPathManager.getSpecialPathInfo(new StoreKey(packageType,  StoreType.valueOf(type), name), path);
        logger.info("info: {}", info);
        return info;
    }
}
