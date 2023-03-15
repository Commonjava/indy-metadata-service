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

import static org.commonjava.service.metadata.handler.PathUtils.normalize;
import static org.commonjava.service.metadata.handler.PathUtils.parentPath;

public class MetadataUtil
{

    public static final String METADATA_NAME = "maven-metadata.xml";

    public static final String NPM_METADATA_NAME = "package.json";

    public static final String MERGEINFO_SUFFIX = ".info";

    public static final String SHA_SUFFIX = ".sha";

    public static final String MD5_SUFFIX = ".md5";

    /**
     * Get the path/to/metadata.xml given a pom or jar file path.
     */
    public static String getMetadataPath( String pomPath )
    {
        final String versionPath = normalize( parentPath( pomPath ) );
        return normalize( normalize( parentPath( versionPath ) ), METADATA_NAME );
    }

    public static String getPkgMetadataPath( String tarPath )
    {
        String pkgPath = normalize ( parentPath( normalize( parentPath( tarPath ) ) ));
        return normalize( pkgPath, NPM_METADATA_NAME ) ;
    }
}
