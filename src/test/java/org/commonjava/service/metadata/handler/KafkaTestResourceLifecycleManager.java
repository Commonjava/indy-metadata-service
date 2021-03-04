package org.commonjava.service.metadata.handler;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Use the in-memory connector to avoid having to use a broker.
 */
public class KafkaTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager
{
    @Override
    public Map<String, String> start()
    {
        Map<String, String> env = new HashMap<>();
        Map<String, String> props1 = InMemoryConnector.switchIncomingChannelsToInMemory( "file-event-in");
        env.putAll(props1);
        return env;
    }

    @Override
    public void stop()
    {
        InMemoryConnector.clear();
    }
}
