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

import java.util.ArrayList;
import java.util.List;

public class NPMSpecialPathSet implements SpecialPathSet {
    final List<SpecialPathInfo> npmSpecialPaths = new ArrayList();

    public NPMSpecialPathSet() {
        this.npmSpecialPaths.add(SpecialPathInfo.from(new FilePatternMatcher("package\\.json$")).setMergable(true).setMetadata(true).build());
        this.npmSpecialPaths.add(SpecialPathInfo.from(new FilePatternMatcher(".*(\\.md5|\\.sha[\\d]+)$")).setDecoratable(false).setMergable(true).setMetadata(true).build());
    }

    public String getPackageType() {
        return "npm";
    }

    public List<SpecialPathInfo> getSpecialPathInfos() {
        return this.npmSpecialPaths;
    }

    public synchronized void registerSpecialPathInfo(SpecialPathInfo pathInfo) {
        this.npmSpecialPaths.add(pathInfo);
    }

    public synchronized void deregisterSpecialPathInfo(SpecialPathInfo pathInfo) {
        this.npmSpecialPaths.remove(pathInfo);
    }
}
