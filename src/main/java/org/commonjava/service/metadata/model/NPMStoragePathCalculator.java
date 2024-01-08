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
package org.commonjava.service.metadata.model;

import org.apache.commons.io.FilenameUtils;
import org.commonjava.service.metadata.handler.SpecialPathConstants;
import org.commonjava.service.metadata.handler.SpecialPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static org.commonjava.service.metadata.handler.MetadataUtil.NPM_METADATA_NAME;
import static org.commonjava.service.metadata.handler.PathUtils.normalize;
import static org.commonjava.service.metadata.handler.SpecialPathConstants.PKG_TYPE_NPM;

@ApplicationScoped
public class NPMStoragePathCalculator
        implements StoragePathCalculator
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    SpecialPathManager specialPathManager;

    protected NPMStoragePathCalculator()
    {
    }

    public NPMStoragePathCalculator( SpecialPathManager specialPathManager )
    {
        this.specialPathManager = specialPathManager;
    }

    @Override
    public String calculateStoragePath( final StoreKey key, final String path )
    {

        if ( PKG_TYPE_NPM.equals( key.getPackageType() ) )
        {
            String pkg = path;

            final String extension = getSpecialPathExt( path );
            if ( extension != null )
            {
                pkg = path.substring( 0, path.indexOf( extension ) );
            }

            if ( pkg.startsWith( "/" ) )
            {
                pkg = pkg.substring( 1 );
            }

            // This is considering the single path for npm standard like "/jquery"
            final boolean isSinglePath = !pkg.startsWith( "@" ) && pkg.split( "/" ).length == 1;
            // This is considering the scoped path for npm standard like "/@type/jquery"
            final boolean isScopedPath = pkg.startsWith( "@" ) && pkg.split( "/" ).length == 2;
            if ( isSinglePath || isScopedPath )
            {
                logger.debug( "Modifying target path: {}, appending '{}', store {}", path, NPM_METADATA_NAME,
                        key );
                return extension != null ?
                        normalize( pkg, NPM_METADATA_NAME + extension ) :
                        normalize( pkg, NPM_METADATA_NAME );
            }
        }

        return path;
    }

    private String getSpecialPathExt( String path )
    {

        SpecialPathInfo spi = specialPathManager.getSpecialPathInfo( path, PKG_TYPE_NPM );

        if ( spi != null && spi.isMetadata() )
        {
            return path.endsWith( SpecialPathConstants.HTTP_METADATA_EXT ) ?
                    SpecialPathConstants.HTTP_METADATA_EXT :
                    "." + FilenameUtils.getExtension( path );
        }
        return null;
    }

}
