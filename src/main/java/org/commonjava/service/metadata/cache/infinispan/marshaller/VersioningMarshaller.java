package org.commonjava.service.metadata.cache.infinispan.marshaller;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;
import java.util.ArrayList;

public class VersioningMarshaller implements MessageMarshaller<Versioning>
{
    @Override
    public Versioning readFrom( ProtoStreamReader reader ) throws IOException
    {
        Versioning versioning = new Versioning();
        versioning.setLatest( reader.readString( "latest" ) );
        versioning.setRelease( reader.readString( "release" ) );
        versioning.setSnapshot( reader.readObject( "snapshot", Snapshot.class ) );
        versioning.setVersions( reader.readCollection( "versions", new ArrayList<String>(), String.class ) );
        versioning.setLastUpdated( reader.readString( "lastUpdated" ) );
        versioning.setSnapshotVersions( reader.readCollection( "snapshotVersions", new ArrayList<SnapshotVersion>(), SnapshotVersion.class ) );
        return versioning;
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, Versioning versioning ) throws IOException
    {
        writer.writeString( "latest", versioning.getLatest() );
        writer.writeString( "release", versioning.getRelease() );
        writer.writeObject( "snapshot", versioning.getSnapshot(), Snapshot.class);
        writer.writeCollection( "versions", versioning.getVersions(), String.class );
        writer.writeString( "lastUpdated", versioning.getLastUpdated() );
        writer.writeCollection( "snapshotVersions", versioning.getSnapshotVersions(), SnapshotVersion.class );
    }

    @Override
    public Class<? extends Versioning> getJavaClass()
    {
        return Versioning.class;
    }

    @Override
    public String getTypeName()
    {
        return "metadata_info.Versioning";
    }
}
