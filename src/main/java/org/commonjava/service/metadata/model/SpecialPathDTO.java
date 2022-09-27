package org.commonjava.service.metadata.model;

import java.util.Collection;

public class SpecialPathDTO
{

    Collection<SpecialPathInfo> stdSpecialPaths;

    Collection<SpecialPathSet> specialPathSets;

    public Collection<SpecialPathInfo> getStdSpecialPaths() {
        return stdSpecialPaths;
    }

    public void setStdSpecialPaths(Collection<SpecialPathInfo> stdSpecialPaths) {
        this.stdSpecialPaths = stdSpecialPaths;
    }

    public Collection<SpecialPathSet> getSpecialPathSets() {
        return specialPathSets;
    }

    public void setSpecialPathSets(Collection<SpecialPathSet> specialPathSets) {
        this.specialPathSets = specialPathSets;
    }
}
