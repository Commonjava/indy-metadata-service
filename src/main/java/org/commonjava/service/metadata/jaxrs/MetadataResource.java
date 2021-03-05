package org.commonjava.service.metadata.jaxrs;

import org.commonjava.service.metadata.controller.MetadataController;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.PATH;

@Path( "/api/metadata" )
public class MetadataResource
{

    @Inject
    MetadataController controller;

    @Inject
    ResponseHelper responseHelper;

    @GET
    @Path( "/{packageType}/{type: (hosted|group|remote)}/{name}/{path: (.*)}" )
    @Produces( APPLICATION_JSON )
    public Response doGet(
                    final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                                    "remote" } ), required = true ) @PathParam( "type" ) String type,
                    final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                    @PathParam( "path" ) String path )
    {
        MetadataInfo info = controller.getMetadataInfo( packageType, type, name, path );
        return responseHelper.formatOkResponseWithJsonEntity( info );
    }

}
