package org.commonjava.service.metadata.handler;


import org.apache.commons.lang3.StringUtils;

public final class PathUtils {

    private static final String[] ROOT_ARRY = new String[]{"/"};

    private PathUtils() {
    }

    public static String[] parentPath(String path) {
        String[] parts = path.split("/");
        if (parts.length < 2) {
            return ROOT_ARRY;
        } else {
            String[] parentParts = new String[parts.length - 1];
            System.arraycopy(parts, 0, parentParts, 0, parentParts.length);
            return parentParts;
        }
    }

    public static String normalize(String... path) {
        if (path != null && path.length >= 1 && (path.length != 1 || path[0] != null)) {
            return StringUtils.join(path, "/");
        } else {
            return "/";
        }
    }

    public static String join( final String base, final String... parts )
    {
        if ( parts.length < 1 )
        {
            return base;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( base );

        for ( final String part : parts )
        {
            final String[] subParts = part.split( "/" );
            for ( final String subPart : subParts )
            {
                final String normal = normalizePathPart( subPart );
                if ( normal.length() < 1 )
                {
                    continue;
                }

                sb.append( "/" )
                        .append( normal );
            }
        }

        return sb.toString();
    }

    public static String normalizePathPart( final String path )
    {
        String result = path.trim();
        while ( result.startsWith( "/" ) && result.length() > 1 )
        {
            result = result.substring( 1 );
        }

        return result.replace( '\\', '/' );
    }
}
