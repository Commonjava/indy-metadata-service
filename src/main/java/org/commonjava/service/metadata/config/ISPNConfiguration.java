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
