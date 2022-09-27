package org.commonjava.service.metadata.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.commonjava.service.metadata.handler.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.commonjava.service.metadata.model.PathStyle.hashed;

@ApplicationScoped
public class IndyPathGenerator
        implements PathGenerator
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private Instance<StoragePathCalculator> injectedStoragePathCalculators;

    private Set<StoragePathCalculator> pathCalculators;

    public IndyPathGenerator(){}

    public IndyPathGenerator( Set<StoragePathCalculator> pathCalculators )
    {
        this.pathCalculators = pathCalculators;
    }

    @PostConstruct
    public void postConstruct()
    {
        pathCalculators = new HashSet<>();
        if ( !injectedStoragePathCalculators.isUnsatisfied() )
        {
            injectedStoragePathCalculators.forEach( pathCalculators::add );
        }
    }

    @Override
    public String getFilePath( final StoreKey key, final String path )
    {

        final String name = key.getPackageType() + "/" + key.getType()
                .name() + "-" + key.getName();

        return PathUtils.join( name, path );
    }

    @Override
    public String getPath( final StoreKey key, final String path, final PathStyle pathStyle )
    {
        if ( hashed == pathStyle )
        {
            File f = new File( path );
            String dir = f.getParent();
            if ( dir == null )
            {
                dir = "/";
            }

            if ( dir.length() > 1 && dir.startsWith( "/" ) )
            {
                dir = dir.substring( 1 );
            }

            String digest = DigestUtils.sha256Hex( dir );

            logger.trace( "Using SHA-256 digest: '{}' for dir: '{}' of path: '{}'", digest, dir, path );

            // Format examples:
            // - aa/bb/aabbccddeeff001122/simple-1.0.pom
            // - aa/bb/aabbccddeeff001122/gulp-size
            // - 00/11/001122334455667788/gulp-size-1.3.0.tgz
            return String.format( "%s/%s/%s/%s", digest.substring( 0, 2 ), digest.substring( 2, 4 ), digest, f.getName() );
        }
        else
        {
            AtomicReference<String> pathref = new AtomicReference<>( path );
            pathCalculators.forEach( c -> pathref.set( c.calculateStoragePath( key, pathref.get() ) ) );

            return pathref.get();
        }

    }

}
