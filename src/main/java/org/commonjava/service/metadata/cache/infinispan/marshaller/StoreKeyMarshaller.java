package org.commonjava.service.metadata.cache.infinispan.marshaller;


import org.commonjava.service.metadata.model.StoreKey;
import org.commonjava.service.metadata.model.StoreType;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class StoreKeyMarshaller implements MessageMarshaller<StoreKey>
{
    @Override
    public StoreKey readFrom( MessageMarshaller.ProtoStreamReader reader ) throws IOException
    {
        String packageType = reader.readString( "packageType" );
        StoreType storeType = reader.readEnum( "type", StoreType.class );
        String name = reader.readString( "name" );
        return new StoreKey( packageType, storeType, name );
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, StoreKey storeKey ) throws IOException
    {
        writer.writeString( "packageType", storeKey.getPackageType() );
        writer.writeEnum( "type", storeKey.getType());
        writer.writeString( "name", storeKey.getName() );
    }

    @Override
    public Class<? extends StoreKey> getJavaClass()
    {
        return StoreKey.class;
    }

    @Override
    public String getTypeName()
    {
        return "metadata_key.StoreKey";
    }
}
