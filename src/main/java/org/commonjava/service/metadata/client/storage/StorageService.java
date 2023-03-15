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
