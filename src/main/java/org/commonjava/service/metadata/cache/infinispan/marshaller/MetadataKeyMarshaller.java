package org.commonjava.service.metadata.cache.infinispan.marshaller;


import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class MetadataKeyMarshaller implements MessageMarshaller<MetadataKey>
{
    @Override
    public MetadataKey readFrom( ProtoStreamReader reader ) throws IOException
    {
        StoreKey storeKey = reader.readObject( "storeKey", StoreKey.class );
        String path = reader.readString( "path" );
        return new MetadataKey( storeKey, path );
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, MetadataKey metadataKey ) throws IOException
    {
        writer.writeObject( "storeKey", metadataKey.getStoreKey(), StoreKey.class);
        writer.writeString( "path", metadataKey.getPath() );
    }

    @Override
    public Class<? extends MetadataKey> getJavaClass()
    {
        return MetadataKey.class;
    }

    @Override
    public String getTypeName()
    {
        return "metadata_key.MetadataKey";
    }
}
