package org.commonjava.service.metadata.handler;

import org.commonjava.service.metadata.model.*;

import java.util.ArrayList;
import java.util.List;

public class SpecialPathConstants {
    public static final String PKG_TYPE_MAVEN = "maven";
    public static final String PKG_TYPE_NPM = "npm";
    public static final String PKG_TYPE_GENERIC_HTTP = "generic-http";
    public static final String HTTP_METADATA_EXT = ".http-metadata.json";
    public static final List<SpecialPathInfo> STANDARD_SPECIAL_PATHS;
    public static final SpecialPathInfo DEFAULT_FILE = SpecialPathInfo.from(new PathPatternMatcher(".*[^/]")).setDecoratable(true).setDeletable(true).setListable(false).setMergable(false).setMetadata(false).setPublishable(true).setRetrievable(true).setStorable(true).build();
    public static final SpecialPathInfo DEFAULT_DIR = SpecialPathInfo.from(new PathPatternMatcher(".*/")).setDecoratable(true).setDeletable(true).setListable(false).setMergable(false).setMetadata(false).setPublishable(true).setRetrievable(true).setStorable(true).build();
    public static final SpecialPathSet MVN_SP_PATH_SET;
    public static final SpecialPathSet NPM_SP_PATH_SET;

    public SpecialPathConstants() {
    }

    static {
        List<SpecialPathInfo> standardSp = new ArrayList();
        SpecialPathInfo pi = SpecialPathInfo.from(new FilePatternMatcher(".*\\.http-metadata\\.json$")).setDecoratable(false).setListable(false).setPublishable(false).setRetrievable(false).setStorable(true).setMetadata(true).setMergable(false).build();
        standardSp.add(pi);
        pi = SpecialPathInfo.from(new FilePatternMatcher("\\.listing\\.txt")).setDecoratable(false).setListable(false).setPublishable(false).setRetrievable(false).setStorable(false).setMergable(true).setMetadata(true).build();
        standardSp.add(pi);
        STANDARD_SPECIAL_PATHS = standardSp;
        MVN_SP_PATH_SET = new MavenSpecialPathSet();
        NPM_SP_PATH_SET = new NPMSpecialPathSet();
    }
}
