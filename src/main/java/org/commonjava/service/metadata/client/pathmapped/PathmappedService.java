package org.commonjava.service.metadata.client.pathmapped;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/api/pathmapped")
@RegisterRestClient(configKey="pathmap-api")
public interface PathmappedService
{
    @DELETE
    @Path("/content/{packageType}/{type: (hosted|group|remote)}/{name}/{path: (.*)}")
    void delete(final @PathParam( "packageType" ) String packageType,
                           final @PathParam( "type" ) String type,
                           final @PathParam( "name" ) String name,
                           final @PathParam( "path" ) String path);

}
