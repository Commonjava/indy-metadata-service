package org.commonjava.service.metadata.handler;

import org.commonjava.service.metadata.model.SpecialPathInfo;
import org.commonjava.service.metadata.model.SpecialPathSet;
import org.commonjava.service.metadata.model.StoreKey;

import java.util.Collection;

public interface SpecialPathManager
{
    void registerSpecialPathInfo(SpecialPathInfo var1);

    void registerSpecialPathInfo(SpecialPathInfo var1, String var2);

    void deregisterSpecialPathInfo(SpecialPathInfo var1);

    void deregisterSpecialPathInfo(SpecialPathInfo var1, String var2);

    void registerSpecialPathSet(SpecialPathSet var1);

    SpecialPathSet deregesterSpecialPathSet(SpecialPathSet var1);

    Collection<SpecialPathSet> getRegisteredSpecialPathSet();

    SpecialPathSet getRegisteredSpecialPathSet(String packageType);

    SpecialPathInfo getSpecialPathInfo(StoreKey var1, String var2);


    SpecialPathInfo getSpecialPathInfo(StoreKey var1, String var2, String var3);

    SpecialPathInfo getSpecialPathInfo(String var1, String var2);

    Collection<SpecialPathInfo> getRegisteredSpecialInfo();
}
