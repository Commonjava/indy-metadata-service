package org.commonjava.service.metadata.handler;


import org.apache.commons.lang3.StringUtils;

public final class PathUtils {

    private static final String[] ROOT_ARRY = new String[]{"/"};

    private PathUtils() {
    }

    public static String[] parentPath(String path) {
        String[] parts = path.split("/");
        if (parts.length < 2) {
            return ROOT_ARRY;
        } else {
            String[] parentParts = new String[parts.length - 1];
            System.arraycopy(parts, 0, parentParts, 0, parentParts.length);
            return parentParts;
        }
    }

    public static String normalize(String... path) {
        if (path != null && path.length >= 1 && (path.length != 1 || path[0] != null)) {
            return StringUtils.join(path, "/");
        } else {
            return "/";
        }
    }
}
