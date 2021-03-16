package org.commonjava.service.metadata.cache;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.commonjava.service.metadata.cache.infinispan.marshaller.MetadataInfoMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.MetadataKeyMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.MetadataMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.PluginMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.StoreKeyMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.StoreTypeMarshaller;
import org.commonjava.service.metadata.cache.infinispan.marshaller.VersioningMarshaller;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.commonjava.service.metadata.model.StoreType;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProtoSchemeTest
{

    @Test
    public void testMetadataInfoProto() throws IOException
    {
        SerializationContext ctx = ProtobufUtil.newSerializationContext();

        ctx.registerProtoFiles( FileDescriptorSource.fromResources( "metadata_info.proto" ) );
        ctx.registerMarshaller( new MetadataInfoMarshaller() );
        ctx.registerMarshaller( new MetadataMarshaller() );
        ctx.registerMarshaller( new VersioningMarshaller() );
        ctx.registerMarshaller( new PluginMarshaller() );

        Metadata metadata = new Metadata();
        metadata.setGroupId( "groupId" );
        metadata.setArtifactId( "artifactId" );
        metadata.setVersion( "1.2" );

        Versioning versioning = new Versioning();
        versioning.setVersions( Arrays.asList("1.0", "1.1", "1.2") );
        metadata.setVersioning( versioning );

        Plugin plugin = new Plugin();
        plugin.setName( "plugin" );
        plugin.setArtifactId( "maven-plugin" );
        metadata.setPlugins( Arrays.asList( plugin ) );

        MetadataInfo info = new MetadataInfo( metadata );

        byte[] bytes = ProtobufUtil.toWrappedByteArray(ctx, info);
        Object out = ProtobufUtil.fromWrappedByteArray(ctx, bytes);

        assertTrue( out instanceof MetadataInfo );
        assertEquals( "groupId", ( (MetadataInfo) out ).getMetadata().getGroupId() );
        assertEquals( "artifactId", ( (MetadataInfo) out ).getMetadata().getArtifactId() );
        assertEquals( "1.2", ( (MetadataInfo) out ).getMetadata().getVersion() );
        assertEquals( 1, ( (MetadataInfo) out ).getMetadata().getPlugins().size() );
        assertEquals( 3, ( (MetadataInfo) out ).getMetadata().getVersioning().getVersions().size() );
        assertTrue( ( (MetadataInfo) out ).getMetadata().getVersioning().getVersions().contains( "1.0" ) );

    }

    @Test
    public void testMetadataKeyProto() throws IOException
    {

        SerializationContext ctx = ProtobufUtil.newSerializationContext();

        ctx.registerProtoFiles( FileDescriptorSource.fromResources( "metadata_key.proto" ) );
        ctx.registerMarshaller( new MetadataKeyMarshaller() );
        ctx.registerMarshaller( new StoreKeyMarshaller() );
        ctx.registerMarshaller( new StoreTypeMarshaller() );

        StoreKey storeKey = new StoreKey( "maven", StoreType.remote, "central");
        MetadataKey metadataKey = new MetadataKey( storeKey, "org/test.pom" );

        byte[] bytes = ProtobufUtil.toWrappedByteArray(ctx, metadataKey);
        Object out = ProtobufUtil.fromWrappedByteArray(ctx, bytes);

        assertTrue( out instanceof MetadataKey );
        assertEquals( "maven:remote:central", ( (MetadataKey) out ).getStoreKey().toString() );
        assertEquals( "org/test.pom", ( (MetadataKey) out ).getPath() );

    }

}
