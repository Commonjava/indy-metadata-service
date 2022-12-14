package org.commonjava.service.metadata.client.storage;

import org.commonjava.event.storage.BatchCleanupRequest;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/api/storage")
@RegisterRestClient(configKey="storage-service-api")
public interface StorageService
{
    @DELETE
    @Path("/content/{filesystem}/{path: (.*)}")
    Response delete(final @PathParam( "filesystem" ) String filesytem, final @PathParam( "path" ) String path);

    @POST
    @Path("/filesystem/cleanup")
    Response cleanup(final BatchCleanupRequest request );

}
