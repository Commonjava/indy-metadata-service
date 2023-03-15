/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.metadata;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.commonjava.service.metadata.cache.MetadataCacheManager;
import org.commonjava.service.metadata.model.MetadataInfo;
import org.commonjava.service.metadata.model.MetadataKey;
import org.commonjava.service.metadata.model.StoreKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class MetadataResourceTest
{

    @Inject
    MetadataCacheManager cacheManager;

    private final String PACKAGE_TYPE = "maven";
    private final String TYPE = "remote";
    private final String NAME = "central";
    private final String PATH = "org/commonjava/indy-metadata-service/1.0/maven-metadata.xml";

    @BeforeEach
    public void init()
    {
        String storeKeyStr = PACKAGE_TYPE + ":" + TYPE + ":" + NAME;
        MetadataKey key = new MetadataKey( StoreKey.fromString( storeKeyStr ), PATH );
        Metadata metadata = new Metadata();
        metadata.setGroupId( "org.commonjava" );
        metadata.setArtifactId( "indy-metadata-service" );
        metadata.setVersion( "1.0-SNAPSHOT" );
        cacheManager.put( key, new MetadataInfo( metadata ) );
    }

    @Test
    public void testGetMetadataInfo()
    {
        Response response = given().pathParam( "packageType", PACKAGE_TYPE )
                                   .pathParam( "type", TYPE )
                                   .pathParam( "name", NAME )
                                   .pathParam( "path", PATH )
                                   .when()
                                   .get( "/api/metadata/{packageType}/{type}/{name}/{path}" )
                                   .then()
                                   .extract().response();

        assertEquals( 200, response.statusCode() );
        assertEquals( "org.commonjava", response.jsonPath().getObject( "metadata", Metadata.class ).getGroupId() );
        assertEquals( "indy-metadata-service", response.jsonPath().getObject( "metadata", Metadata.class ).getArtifactId() );
    }

    @Test
    public void testGetAllPaths()
    {
        Response response = given().pathParam( "packageType", PACKAGE_TYPE )
                                   .pathParam( "type", TYPE )
                                   .pathParam( "name", NAME )
                                   .when()
                                   .get( "/api/metadata/{packageType}/{type}/{name}" )
                                   .then()
                                   .extract().response();

        assertEquals( 200, response.statusCode() );
        assertTrue( response.jsonPath().getObject( ".", Set.class ).contains( PATH ) );

    }
}
