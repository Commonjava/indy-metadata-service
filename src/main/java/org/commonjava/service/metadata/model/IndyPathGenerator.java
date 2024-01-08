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
package org.commonjava.service.metadata.model;

import org.commonjava.indy.model.util.DefaultPathGenerator;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.commonjava.indy.model.core.PathStyle.base64url;
import static org.commonjava.indy.model.core.PathStyle.hashed;
import org.commonjava.indy.model.core.PathStyle;

@ApplicationScoped
public class IndyPathGenerator
{
    @Inject
    private Instance<StoragePathCalculator> injectedStoragePathCalculators;

    private Set<StoragePathCalculator> pathCalculators;

    private DefaultPathGenerator defaultPathGenerator = new DefaultPathGenerator();

    public IndyPathGenerator(){}

    public IndyPathGenerator( Set<StoragePathCalculator> pathCalculators )
    {
        this.pathCalculators = pathCalculators;
    }

    @PostConstruct
    public void postConstruct()
    {
        pathCalculators = new HashSet<>();
        if ( !injectedStoragePathCalculators.isUnsatisfied() )
        {
            injectedStoragePathCalculators.forEach( pathCalculators::add );
        }
    }

    public String getPath( final StoreKey key, final String path, final PathStyle pathStyle )
    {
        if ( hashed == pathStyle || base64url == pathStyle)
        {
            return defaultPathGenerator.getStyledPath( path, pathStyle );
        }
        else
        {
            AtomicReference<String> pathref = new AtomicReference<>( path );
            pathCalculators.forEach( c -> pathref.set( c.calculateStoragePath( key, pathref.get() ) ) );

            return pathref.get();
        }
    }

}
