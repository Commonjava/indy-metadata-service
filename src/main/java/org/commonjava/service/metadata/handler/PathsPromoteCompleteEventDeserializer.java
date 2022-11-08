package org.commonjava.service.metadata.handler;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import org.commonjava.event.promote.PathsPromoteCompleteEvent;

public class PathsPromoteCompleteEventDeserializer extends ObjectMapperDeserializer<PathsPromoteCompleteEvent>
{

    public PathsPromoteCompleteEventDeserializer( )
    {
        super( PathsPromoteCompleteEvent.class );
    }

}
