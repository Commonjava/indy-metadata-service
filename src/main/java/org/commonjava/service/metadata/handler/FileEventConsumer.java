package org.commonjava.service.metadata.handler;

import org.commonjava.event.file.FileEvent;
import org.commonjava.service.metadata.client.pathmapped.PathmappedService;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FileEventConsumer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    @RestClient
    PathmappedService pathmappedService;

    @Incoming("file-event-in")
    public void receive( FileEvent event) {

        logger.info("Got an event: {}", event.getTrackingID());

        //TODO talk to pathmappedService
        // pathmappedService.delete(  );
    }

}
