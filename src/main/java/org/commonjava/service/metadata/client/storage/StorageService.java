package org.commonjava.service.metadata.client.storage;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/api/storage")
@RegisterRestClient(configKey="storage-service-api")
public interface StorageService
{
    @DELETE
    @Path("/content/{filesystem}/{path: (.*)}")
    void delete(final @PathParam( "filesystem" ) String filesytem, final @PathParam( "path" ) String path);

}
