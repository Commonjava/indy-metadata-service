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
