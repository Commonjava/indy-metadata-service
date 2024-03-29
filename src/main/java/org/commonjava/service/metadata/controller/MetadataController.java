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
package org.commonjava.service.metadata.controller;

import org.commonjava.service.metadata.handler.MetadataHandler;
import org.commonjava.service.metadata.handler.SpecialPathManager;
import org.commonjava.service.metadata.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.Set;

@ApplicationScoped
public class MetadataController
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    MetadataHandler metadataHandler;

    @Inject
    SpecialPathManager specialPathManager;

    public MetadataInfo getMetadataInfo( String packageType, String type, String name, String path )
    {

        // TODO actually this is not used now and let's implement it later

        return null;
    }

    public Set<String> getAllPaths( String packageType, String type, String name )
    {

        // TODO actually this is not used now and let's implement it later

        return null;
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
