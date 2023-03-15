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
