package org.commonjava.service.metadata.cache;

import io.quarkus.test.junit.QuarkusTest;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class MetadataCacheManagerTest
{
    @Inject
    MetadataCacheManager metadataCacheManager;

    @Test
    public void query()
    {
        final MetadataInfo info = new MetadataInfo( null );
        StoreKey hosted = StoreKey.fromString( "maven:hosted:test" );
        StoreKey remote = StoreKey.fromString( "maven:remote:test" );

        Set<String> paths = new HashSet<>();
        for ( int i = 0; i < 20; i++ )
        {
            paths.add( "path/to/" + i );
        }

        paths.forEach( p -> {
            metadataCacheManager.put( new MetadataKey( hosted, p ), info );
            metadataCacheManager.put( new MetadataKey( remote, p ), info );
        } );

        String somePath = "path/to/3";

        MetadataInfo ret = metadataCacheManager.get( new MetadataKey( hosted, somePath ) );
        assertNotNull( ret );

        Set<String> allPaths = metadataCacheManager.getAllPaths( hosted );
        assertTrue( allPaths.size() == 20 );

        metadataCacheManager.removeAll( hosted );
        allPaths = metadataCacheManager.getAllPaths( hosted );
        assertTrue( allPaths.size() == 0 );

        ret = metadataCacheManager.get( new MetadataKey( hosted, somePath ) );
        assertNull( ret );

        ret = metadataCacheManager.get( new MetadataKey( remote, somePath ) );
        assertNotNull( ret );

    }

}
