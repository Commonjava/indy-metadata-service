package org.commonjava.service.metadata.client.repository;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/api/stores/query")
@RegisterRestClient(configKey="repo-service-api")
public interface RepositoryService
{

    @GET
    @Path( "/affectedBy" )
    StoreListingDTO<ArtifactStore> getGroupsAffectedBy( final @QueryParam( "keys" ) String keys );

}
