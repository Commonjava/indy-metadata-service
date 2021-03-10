package org.commonjava.service.metadata.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.commonjava.service.metadata.handler.PathUtils.normalize;
import static org.commonjava.service.metadata.handler.PathUtils.parentPath;

public class PathUtilTest
{

    @Test
    public void test()
    {
        String pomPath = "org/commomjava/test-1.pom";
        String parentPath = normalize( parentPath( pomPath ) ) ;
        Assertions.assertEquals( "org/commomjava", parentPath );
    }

}
