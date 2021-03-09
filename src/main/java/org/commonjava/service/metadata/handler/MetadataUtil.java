package org.commonjava.service.metadata.handler;

import static org.commonjava.service.metadata.handler.PathUtils.normalize;
import static org.commonjava.service.metadata.handler.PathUtils.parentPath;

public class MetadataUtil
{

    public static final String METADATA_NAME = "maven-metadata.xml";

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
}
