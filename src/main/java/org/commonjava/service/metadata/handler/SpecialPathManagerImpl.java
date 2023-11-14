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

import org.apache.commons.lang3.StringUtils;
import org.commonjava.service.metadata.model.*;
import org.commonjava.indy.model.core.PathStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SpecialPathManagerImpl implements SpecialPathManager {
    private static final Logger logger = LoggerFactory.getLogger(SpecialPathManagerImpl.class);
    private List<SpecialPathInfo> stdSpecialPaths;
    private Map<String, SpecialPathSet> pkgtypes;
    @Inject
    private IndyPathGenerator pathGenerator;

    public SpecialPathManagerImpl() {
        this.initPkgPathSets();
    }

    public SpecialPathManagerImpl(IndyPathGenerator pathGenerator) {
        this();
        this.pathGenerator = pathGenerator;
    }

    @PostConstruct
    public void initPkgPathSets() {
        this.stdSpecialPaths = new ArrayList();
        this.stdSpecialPaths.addAll(SpecialPathConstants.STANDARD_SPECIAL_PATHS);
        this.pkgtypes = new ConcurrentHashMap();
        this.pkgtypes.put(SpecialPathConstants.MVN_SP_PATH_SET.getPackageType(), SpecialPathConstants.MVN_SP_PATH_SET);
        this.pkgtypes.put(SpecialPathConstants.NPM_SP_PATH_SET.getPackageType(), SpecialPathConstants.NPM_SP_PATH_SET);
    }

    public synchronized void registerSpecialPathInfo(SpecialPathInfo pathInfo) {
        this.stdSpecialPaths.add(pathInfo);
    }

    public void registerSpecialPathInfo(SpecialPathInfo pathInfo, String pkgType) {
        (this.pkgtypes.get(pkgType)).registerSpecialPathInfo(pathInfo);
    }

    public synchronized void deregisterSpecialPathInfo(SpecialPathInfo pathInfo) {
        this.stdSpecialPaths.remove(pathInfo);
    }

    public void deregisterSpecialPathInfo(SpecialPathInfo pathInfo, String pkgType) {
        (this.pkgtypes.get(pkgType)).deregisterSpecialPathInfo(pathInfo);
    }

    public void registerSpecialPathSet(SpecialPathSet pathSet) {
        if (this.pkgtypes.containsKey(pathSet.getPackageType())) {
            logger.warn("The package types already contains the path set for this package type {}, will override it", pathSet.getPackageType());
        }

        this.pkgtypes.put(pathSet.getPackageType(), pathSet);
        if (logger.isTraceEnabled()) {
            List<SpecialPathMatcher> pathMatchers = new ArrayList();
            Iterator var3 = pathSet.getSpecialPathInfos().iterator();

            while(var3.hasNext()) {
                SpecialPathInfo info = (SpecialPathInfo)var3.next();
                pathMatchers.add(info.getMatcher());
            }

            logger.trace("Enabling special paths for package: '{}'\n  - {}\n\nCalled from: {}", new Object[]{pathSet.getPackageType(), StringUtils.join(pathMatchers, "\n  - "), Thread.currentThread().getStackTrace()[1]});
        }

    }

    public SpecialPathSet deregesterSpecialPathSet(SpecialPathSet pathSet) {
        if (!this.pkgtypes.containsKey(pathSet.getPackageType())) {
            logger.warn("The package does not contain the path set for this package type {}, no deregister operation there", pathSet.getPackageType());
        }

        return this.pkgtypes.remove(pathSet.getPackageType());
    }

    @Override
    public Collection<SpecialPathSet> getRegisteredSpecialPathSet() {
        return this.pkgtypes.values();
    }

    @Override
    public SpecialPathSet getRegisteredSpecialPathSet(String packageType) {
        return this.pkgtypes.get(packageType);
    }

    public SpecialPathInfo getSpecialPathInfo(StoreKey key, String path) {
        if (key != null) {
            SpecialPathInfo info = this.getSpecialPathInfo(key, path, key.getPackageType());
            if (info == null) {
                info = this.getSpecialPathInfo(key, this.getStoragePath(key, path), key.getPackageType());
            }

            return info;
        } else {
            return null;
        }
    }

    public SpecialPathInfo getSpecialPathInfo(StoreKey key, String path, String pkgType) {
        SpecialPathInfo info = this.getPathInfo(key, path, this.stdSpecialPaths);
        if (info != null) {
            return info;
        } else {
            return this.pkgtypes.containsKey(pkgType) ? this.getPathInfo(key, path, (this.pkgtypes.get(pkgType)).getSpecialPathInfos()) : null;
        }
    }

    private SpecialPathInfo getPathInfo(StoreKey key, String path, Collection<SpecialPathInfo> from) {
        SpecialPathInfo firstHit = null;
        if (path != null) {
            Iterator var5 = from.iterator();

            while(true) {
                SpecialPathInfo info;
                boolean originalMatched;
                boolean storagePathMatched;
                do {
                    if (!var5.hasNext()) {
                        return firstHit;
                    }

                    info = (SpecialPathInfo)var5.next();
                    originalMatched = info.getMatcher().matches(key, path);
                    storagePathMatched = originalMatched;
                    if (key != null) {
                        storagePathMatched = info.getMatcher().matches(key, this.getStoragePath(key, path));
                    }
                } while(!originalMatched && !storagePathMatched);

                if (firstHit != null) {
                    Logger logger = LoggerFactory.getLogger(this.getClass());
                    logger.error("Duplicate special-path registration for: {}:{}. Using: {}", new Object[]{key, path, firstHit});
                } else {
                    firstHit = info;
                }
            }
        } else {
            return firstHit;
        }
    }

    public SpecialPathInfo getSpecialPathInfo(String path, String pkgType) {
        return path == null ? null : this.getSpecialPathInfo(null, path, pkgType);
    }

    @Override
    public Collection<SpecialPathInfo> getRegisteredSpecialInfo() {
        return stdSpecialPaths;
    }

    private String getStoragePath(StoreKey key, String path) {
        String storagePath = path;
        if (this.pathGenerator != null)
        {
            storagePath = this.pathGenerator.getPath(key, path, PathStyle.plain);
        }

        logger.info("storagePath: {}, path: {}", storagePath, path);

        return storagePath;
    }
}
