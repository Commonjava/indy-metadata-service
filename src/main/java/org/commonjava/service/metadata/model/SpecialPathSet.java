package org.commonjava.service.metadata.model;

import java.util.List;

public interface SpecialPathSet
{
    List<SpecialPathInfo> getSpecialPathInfos();

    void registerSpecialPathInfo(SpecialPathInfo var1);

    void deregisterSpecialPathInfo(SpecialPathInfo var1);

    String getPackageType();
}
