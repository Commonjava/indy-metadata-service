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
