package org.commonjava.service.metadata.cache.infinispan.marshaller;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class SnapshotMarshaller implements MessageMarshaller<Snapshot>
{
    @Override
    public Snapshot readFrom( ProtoStreamReader reader ) throws IOException
    {
        Snapshot snapshot = new Snapshot();
        snapshot.setTimestamp( reader.readString( "timestamp" ) );
        snapshot.setBuildNumber( reader.readInt( "buildNumber" ) );
        snapshot.setLocalCopy( reader.readBoolean( "localCopy" ) );
        return snapshot;
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, Snapshot snapshot ) throws IOException
    {
        writer.writeString( "timestamp", snapshot.getTimestamp() );
        writer.writeInt( "buildNumber", snapshot.getBuildNumber());
        writer.writeBoolean( "localCopy", snapshot.isLocalCopy());
    }

    @Override
    public Class<? extends Snapshot> getJavaClass()
    {
        return Snapshot.class;
    }

    @Override
    public String getTypeName()
    {
        return "metadata_info.Snapshot";
    }
}
