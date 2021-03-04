package org.commonjava.service.metadata.cache.infinispan.marshaller;

import org.commonjava.service.metadata.model.StoreType;
import org.infinispan.protostream.EnumMarshaller;

public class StoreTypeMarshaller implements EnumMarshaller<StoreType>
{

    @Override
    public StoreType decode( int enumValue )
    {
        if ( enumValue == 0 )
        {
            return StoreType.group;
        }
        else if ( enumValue == 1 )
        {
            return StoreType.remote;
        }
        else
        {
            return StoreType.hosted;
        }
    }

    @Override
    public int encode( StoreType storeType ) throws IllegalArgumentException
    {
        if ( storeType.equals( StoreType.group ) )
        {
            return 0;
        }
        else if ( storeType.equals( StoreType.remote ) )
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }

    @Override
    public Class<? extends StoreType> getJavaClass()
    {
        return StoreType.class;
    }

    @Override
    public String getTypeName()
    {
        return "metadata_key.StoreKey.StoreType";
    }


}
