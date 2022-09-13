package org.commonjava.service.metadata.handler;

import io.quarkus.test.Mock;
import org.commonjava.service.metadata.client.storage.StorageService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;

@Mock
@RestClient
public class MockableStorageService
                implements StorageService
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public void delete( final @PathParam( "filesystem" ) String filesystem, final @PathParam( "path" ) String path )
    {
        logger.info( "Invoke pathMap storage service delete: filesystem: {},path: {}", filesystem, path );
    }

}

