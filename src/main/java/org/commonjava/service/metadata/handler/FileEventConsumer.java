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
import org.commonjava.event.file.FileEvent;
import org.commonjava.event.file.FileEventType;
import org.commonjava.service.metadata.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static org.commonjava.service.metadata.handler.MetadataUtil.getMetadataPath;
import static org.commonjava.service.metadata.handler.MetadataUtil.getPkgMetadataPath;
import static org.commonjava.service.metadata.model.StoreType.hosted;

@ApplicationScoped
public class FileEventConsumer
{

    private static final String POM_EXTENSION = ".pom";

    private static final String PACKAGE_TARBALL_EXTENSION = ".tgz";
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    MetadataHandler metadataHandler;

    /**
     * this observes the channel of {@link FileEvent} for a pom file, which means maven-metadata.xml will be cleared
     * when a version (pom) is uploaded or removed.
     */
    @Blocking
    @Incoming("file-event-in")
    public CompletionStage<Void> receive( Message<FileEvent> message ) {

        FileEvent event = message.getPayload();

        logger.info("Got an event: {}", event);

        if ( event.getEventType().equals( FileEventType.STORAGE ) || event.getEventType()
                                                                          .equals( FileEventType.DELETE ) )
        {
            String path = event.getTargetPath();

            if ( !path.endsWith( POM_EXTENSION ) && !path.endsWith( PACKAGE_TARBALL_EXTENSION ) )
            {
                return message.ack();
            }

            final String keyStr = event.getStoreKey();
            final StoreKey key =  StoreKey.fromString( keyStr );

            if ( hosted != key.getType() )
            {
                return message.ack();
            }

            String clearPath = null;

            if ( path.endsWith( POM_EXTENSION ) )
            {
                clearPath = getMetadataPath( path );

                logger.info( "Pom file {} {}, will clean matched metadata file {}, store: {}", path, event.getEventType(), clearPath, keyStr );

            }
            else if ( path.endsWith( PACKAGE_TARBALL_EXTENSION ) )
            {
                clearPath = getPkgMetadataPath( path );

                logger.info( "Tar file {} {}, will clean matched metadata file {}, store: {}", path, event.getEventType(), clearPath, keyStr );
            }

            if ( hosted == key.getType() )
            {
                metadataHandler.doDelete(key, clearPath);
            }

        }
        return message.ack();
    }

}
