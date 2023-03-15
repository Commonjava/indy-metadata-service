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


import java.io.Serializable;
import java.util.Objects;

public final class MetadataKey
                implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final StoreKey storeKey;

    private final String path;

    public MetadataKey( StoreKey storeKey, String path )
    {
        this.storeKey = storeKey;
        this.path = path;
    }

    public StoreKey getStoreKey()
    {
        return storeKey;
    }

    public String getPath()
    {
        return path;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        MetadataKey that = (MetadataKey) o;
        return Objects.equals( storeKey, that.storeKey ) && Objects.equals( path, that.path );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( storeKey, path );
    }

    private static final String DELIMIT = ",";

    @Override
    public String toString()
    {
        return storeKey.toString() + DELIMIT + path; // e.g., maven:remote:central,/foo/bar/1.0/bar-1.0.pom
    }

    public static MetadataKey fromString( String str )
    {
        int idx = str.lastIndexOf( DELIMIT );
        String storekey = str.substring( 0, idx );
        String path = str.substring( idx + 1 );
        return new MetadataKey( StoreKey.fromString( storekey ), path );
    }
}
