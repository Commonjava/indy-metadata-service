package org.commonjava.service.metadata.config;

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;

@Startup
@ApplicationScoped
public class ISPNConfiguration
{

    @Inject
    @ConfigProperty( name = "ispn.enabled", defaultValue = "true" )
    public Boolean enabled;

    @Inject
    @ConfigProperty( name = "ispn.hotrod.client.config", defaultValue = "/tmp" )
    public String hotrodClientConfigPath;

    @Inject
    @ConfigProperty( name = "ispn.cache.dir", defaultValue = "/tmp" )
    public File cacheConfigDir;

    public String getHotrodClientConfigPath()
    {
        return hotrodClientConfigPath;
    }

    public Boolean isEnabled()
    {
        return enabled;
    }

    public File getCacheConfigDir() { return cacheConfigDir; }

    @PostConstruct
    public void testProperties()
    {
        final var logger = LoggerFactory.getLogger( this.getClass() );
        logger.info( "ispn.enabled: {} hotrod.client: {} cache dir: {}", isEnabled(), getHotrodClientConfigPath(), getCacheConfigDir().getPath());
    }

}
