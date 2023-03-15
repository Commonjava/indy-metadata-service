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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MavenSpecialPathSet implements SpecialPathSet {
    final List<SpecialPathInfo> mvnSpecialPaths = new ArrayList();

    public MavenSpecialPathSet() {
        this.mvnSpecialPaths.add(SpecialPathInfo.from(new FilePatternMatcher("maven-metadata\\.xml$")).setMergable(true).setMetadata(true).build());
        this.mvnSpecialPaths.add(SpecialPathInfo.from(new FilePatternMatcher("maven-metadata\\.xml(\\.md5|\\.sha[\\d]+)$")).setDecoratable(false).setMergable(true).setMetadata(true).build());
        this.mvnSpecialPaths.add(SpecialPathInfo.from(new FilePatternMatcher("archetype-catalog\\.xml$")).setMergable(true).setMetadata(true).build());
        this.mvnSpecialPaths.add(SpecialPathInfo.from(new FilePatternMatcher("archetype-catalog\\.xml(\\.md5|\\.sha[\\d]+)$")).setDecoratable(false).setMergable(true).setMetadata(true).build());
        String notMergablePrefix = ".+(?<!(maven-metadata|archetype-catalog)\\.xml)\\.";
        Iterator var2 = Arrays.asList("asc$", "md5$", "sha[\\d]+$").iterator();

        while(var2.hasNext()) {
            String extPattern = (String)var2.next();
            this.mvnSpecialPaths.add(SpecialPathInfo.from(new FilePatternMatcher(notMergablePrefix + extPattern)).setDecoratable(false).build());
        }

    }

    public String getPackageType() {
        return "maven";
    }

    public List<SpecialPathInfo> getSpecialPathInfos() {
        return this.mvnSpecialPaths;
    }

    public synchronized void registerSpecialPathInfo(SpecialPathInfo pathInfo) {
        this.mvnSpecialPaths.add(pathInfo);
    }

    public synchronized void deregisterSpecialPathInfo(SpecialPathInfo pathInfo) {
        this.mvnSpecialPaths.remove(pathInfo);
    }
}
