package org.commonjava.service.metadata.model;

public interface PathGenerator
{
    String getFilePath(StoreKey var1, String var2);

    default String getPath(StoreKey store, String path, PathStyle pathStyle) {
        return path;
    }
}
