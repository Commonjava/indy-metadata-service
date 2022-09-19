/*
package org.commonjava.service.metadata.handler;

import io.quarkus.test.Mock;
import org.apache.http.HttpStatus;
import org.commonjava.service.metadata.client.storage.StorageService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Mock
@RestClient
public class MockableStorageService
                implements StorageService
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public Response delete(final @PathParam( "filesystem" ) String filesystem, final @PathParam( "path" ) String path )
    {
        logger.info( "Invoke pathMap storage service delete: filesystem: {},path: {}", filesystem, path );
        return Response.status(HttpStatus.SC_OK).build();
    }

}

*/
