package org.commonjava.service.metadata.handler;

import io.quarkus.test.Mock;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.RepositoryService;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.List;

@Mock
@RestClient
public class MockableRepositoryService implements RepositoryService
{

    public final Logger logger = LoggerFactory.getLogger( getClass() );

    public StoreListingDTO<ArtifactStore> getGroupsAffectedBy( final @QueryParam( "keys" ) String keys )
    {
        logger.info( "Invoke Indy repository service, keys: {}", keys );
        ArtifactStore store = new ArtifactStore();
        store.key = StoreKey.fromString( "maven:group:public" );
        StoreListingDTO<ArtifactStore> dto = new StoreListingDTO<>();
        List<ArtifactStore> list = Arrays.asList( store );
        dto.items = list;
        return dto;
    }
}
