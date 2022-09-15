package org.commonjava.service.metadata.client;

import io.quarkus.test.junit.QuarkusTest;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

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
