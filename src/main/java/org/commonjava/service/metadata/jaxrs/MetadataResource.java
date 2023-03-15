/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.metadata.jaxrs;

import org.apache.http.HttpStatus;
import org.commonjava.service.metadata.controller.MetadataController;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.SpecialPathDTO;
import org.commonjava.service.metadata.model.SpecialPathInfo;
import org.commonjava.service.metadata.model.SpecialPathSet;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.Collection;
import java.util.Set;

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

    @DELETE
    @Path( "/{packageType}/{type: (hosted|group|remote)}/{name}/{path: (.*)}" )
    @Produces( APPLICATION_JSON )
    public Response doDelete(
            final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
            final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                    "remote" } ), required = true ) @PathParam( "type" ) String type,
            final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
            @PathParam( "path" ) String path )
    {
        boolean deleted = controller.doDelete( packageType, type, name, path );
        return deleted ? Response.status(HttpStatus.SC_NO_CONTENT).build() : Response.status(HttpStatus.SC_BAD_REQUEST).build();
    }

    @GET
    @Path( "/{packageType}/{type: (hosted|group|remote)}/{name}" )
    @Produces( APPLICATION_JSON )
    public Response getAllPaths(
                    final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                                    "remote" } ), required = true ) @PathParam( "type" ) String type,
                    final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name )
    {
        Set<String> paths = controller.getAllPaths( packageType, type, name );
        return responseHelper.formatOkResponseWithJsonEntity( paths );
    }

    @GET
    @Path("/special-paths/{packageType}")
    @Produces( APPLICATION_JSON )
    public  Response getAllSpecialPathSets(final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType)
    {
        SpecialPathDTO specialPathDTO = controller.getSpecialPathSets(packageType);
        return responseHelper.formatOkResponseWithJsonEntity(specialPathDTO);
    }

    @GET
    @Path("/special-path/{packageType}/{type: (hosted|group|remote)}/{name}/{path: (.*)}")
    @Produces( APPLICATION_JSON )
    public  Response getSpecialPathInfo(
            final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
            final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                    "remote" } ), required = true ) @PathParam( "type" ) String type,
            final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
            @PathParam( "path" ) String path
    )
    {
        SpecialPathInfo pathInfo = controller.getSpecialPathInfo(packageType, type, name, path);
        return responseHelper.formatOkResponseWithJsonEntity(pathInfo);
    }

}
