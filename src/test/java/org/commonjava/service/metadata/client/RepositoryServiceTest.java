package org.commonjava.service.metadata.client;

import io.quarkus.test.junit.QuarkusTest;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

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
        StoreListingDTO<ArtifactStore> dto = repositoryService.getGroupsAffectedBy( "maven:remote:central" );
        Assertions.assertNotNull( dto );
        Assertions.assertEquals( 1, dto.items.size() );
        Assertions.assertEquals( AFFECTED_GROUPS, dto.items.get( 0 ).key.toString() );
    }
}
