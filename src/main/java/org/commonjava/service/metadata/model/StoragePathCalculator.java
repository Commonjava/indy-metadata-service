package org.commonjava.service.metadata.model;

public interface StoragePathCalculator
{
    String calculateStoragePath( StoreKey storeKey, String path );
}
