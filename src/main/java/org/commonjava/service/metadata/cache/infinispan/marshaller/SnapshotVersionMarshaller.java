package org.commonjava.service.metadata.cache.infinispan.marshaller;

import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class SnapshotVersionMarshaller implements MessageMarshaller<SnapshotVersion>
{
    @Override
    public SnapshotVersion readFrom( ProtoStreamReader reader ) throws IOException
    {
        SnapshotVersion version = new SnapshotVersion();
        version.setClassifier( reader.readString( "classifier" ) );
        version.setExtension( reader.readString( "extension" ) );
        version.setVersion( reader.readString( "version" ) );
        version.setUpdated( reader.readString( "updated" ) );
        return version;
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, SnapshotVersion snapshotVersion ) throws IOException
    {
        writer.writeString( "classifier", snapshotVersion.getClassifier() );
        writer.writeString( "extension", snapshotVersion.getExtension() );
        writer.writeString( "version", snapshotVersion.getVersion() );
        writer.writeString( "updated", snapshotVersion.getUpdated() );
    }

    @Override
    public Class<? extends SnapshotVersion> getJavaClass()
    {
        return SnapshotVersion.class;
    }

    @Override
    public String getTypeName()
    {
        return "metadata_info.SnapshotVersion";
    }
}
