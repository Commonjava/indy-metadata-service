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
package org.commonjava.service.metadata.handler;

import io.smallrye.common.annotation.Blocking;
import org.commonjava.event.promote.PathsPromoteCompleteEvent;
import org.commonjava.service.metadata.client.repository.ArtifactStore;
import org.commonjava.service.metadata.client.repository.StoreListingDTO;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static org.commonjava.service.metadata.handler.MetadataUtil.getMetadataPath;
import static org.commonjava.service.metadata.handler.MetadataUtil.getPkgMetadataPath;
import static org.commonjava.service.metadata.model.StoreType.hosted;

@ApplicationScoped
public class PromoteEventConsumer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String POM_EXTENSION = ".pom";

    private static final String PACKAGE_TARBALL_EXTENSION = ".tgz";

    @Inject
    MetadataHandler metadataHandler;

    @Blocking
    @Incoming("promote-event-in")
    public CompletionStage<Void> receive( Message<PathsPromoteCompleteEvent> message )
    {

        PathsPromoteCompleteEvent event = message.getPayload();

        logger.info( "Got an event: {}", event );

        final String keyStr = event.getTargetStore();
        final StoreKey key =  StoreKey.fromString( keyStr );

        if ( hosted != key.getType() )
        {
            return message.ack();
        }

        Set<String> clearPaths = new HashSet<>();
        Set<String> filesystems = new HashSet<>();

        try
        {
            addRepoAndAffectedGroups(key, filesystems);

            if (event.isPurgeSource()) {
                String sourceKey = event.getSourceStore();
                addRepoAndAffectedGroups(StoreKey.fromString(sourceKey), filesystems);
            }

            // include both completed and skipped paths, skipped paths can also affect the TOBE-cleaned metadata
            addToClearPaths(clearPaths, event.getSkippedPaths());
            addToClearPaths(clearPaths, event.getCompletedPaths());

            metadataHandler.doBatchDelete(clearPaths, filesystems);
        }
        catch ( Throwable e )
        {
            logger.error( "Failed to handle the PromoteCompleteEvent.", e );
        }
        return message.ack();
    }

    private void addToClearPaths(Set<String> clearPaths, Set<String> paths) {
        if ( paths != null )
        {
            paths.forEach( path -> {
                String clearPath = getCleanPath(path);
                if ( clearPath != null ) {
                    clearPaths.add(clearPath);
                }
            } );
        }
    }

    private String getCleanPath(String path)
    {
        String clearPath;
        if ( path.endsWith( POM_EXTENSION ) )
        {
            clearPath = getMetadataPath( path );
            logger.info( "Pom file {}, will clean matched metadata file {}", path, clearPath );
        }
        else if ( path.endsWith( PACKAGE_TARBALL_EXTENSION ) )
        {
            clearPath = getPkgMetadataPath( path );
            logger.info( "Tar file {}, will clean matched metadata file {}", path, clearPath );
        }
        else
        {
            logger.info( "Ignore the file {}", path );
            clearPath = null;
        }
        return clearPath;
    }

    private void addRepoAndAffectedGroups( StoreKey storeKey, Set<String> filesystems )
    {
        filesystems.add( storeKey.toString() );
        StoreListingDTO<ArtifactStore> groupsAffectedBySource = metadataHandler.getGroupsAffectdBy( storeKey.toString() );
        if ( groupsAffectedBySource != null )
        {
            if (groupsAffectedBySource.items != null && !groupsAffectedBySource.items.isEmpty())
            {
                groupsAffectedBySource.items.forEach(item -> filesystems.add(item.key.toString()));
            }
        }
    }

}
