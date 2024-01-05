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
package org.commonjava.service.metadata.client;

import io.quarkus.test.junit.QuarkusTest;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
public class RepositoryServiceTest
{

    public static final String AFFECTED_GROUPS = "maven:group:public";

    @Inject
    @RestClient
    RepositoryService repositoryService;

    @Test
    public void testGetGroupsAffectedBy()
    {
        Response response = repositoryService.getGroupsAffectedBy( "maven:remote:central" );
        StoreListingDTO<ArtifactStore> dto = response.readEntity(StoreListingDTO.class);
        Assertions.assertNotNull( dto );
        Assertions.assertEquals( 1, dto.items.size() );
        Assertions.assertEquals( AFFECTED_GROUPS, dto.items.get( 0 ).key.toString() );
    }

    @Test
    public void testGetRepository()
    {
        Response response = repositoryService.getStore("maven", "remote", "central");
        Assertions.assertEquals(200, response.getStatus());
        ArtifactStore store = response.readEntity(ArtifactStore.class);
        Assertions.assertEquals("maven:remote:central", store.key.toString());
    }
}
