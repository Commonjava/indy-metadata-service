package org.commonjava.service.metadata.handler;

import io.quarkus.test.Mock;
import org.commonjava.service.metadata.client.pathmapped.PathmappedService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;

@Mock
@RestClient
public class MockablePathmappedService
                implements PathmappedService
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public void delete( final @PathParam( "packageType" ) String packageType, final @PathParam( "type" ) String type,
                        final @PathParam( "name" ) String name, final @PathParam( "path" ) String path )
    {
        logger.info( "Invoke pathMap storage service delete: packageType: {}, type: {}, name: {}, path: {}",
                     packageType, type, name, path );
    }

}

