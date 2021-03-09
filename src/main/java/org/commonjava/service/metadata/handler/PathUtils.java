package org.commonjava.service.metadata.handler;

import org.commonjava.event.common.EventMetadata;

public final class PathUtils {
    public static final String ROOT = "/";
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
            StringBuilder sb = new StringBuilder();
            int idx = 0;
            String[] var3 = path;
            int var4 = path.length;

            label69:
            for(int var5 = 0; var5 < var4; ++var5) {
                String part = var3[var5];
                if (part != null && part.length() >= 1 && !"/".equals(part)) {
                    if (idx == 0 && part.startsWith("file:")) {
                        if (part.length() > 5) {
                            sb.append(part.substring(5));
                        }
                    } else {
                        if (idx > 0) {
                            while(part.charAt(0) == '/') {
                                if (part.length() < 2) {
                                    continue label69;
                                }

                                part = part.substring(1);
                            }
                        }

                        while(part.charAt(part.length() - 1) == '/') {
                            if (part.length() < 2) {
                                continue label69;
                            }

                            part = part.substring(0, part.length() - 1);
                        }

                        if (sb.length() > 0) {
                            sb.append('/');
                        }

                        sb.append(part);
                        ++idx;
                    }
                }
            }

            if (path[path.length - 1] != null && path[path.length - 1].endsWith("/")) {
                sb.append("/");
            }

            return sb.toString();
        } else {
            return "/";
        }
    }

    /** @deprecated */
    @Deprecated
    public static String storagePath(String path, EventMetadata eventMetadata) {
        String storage = (String)eventMetadata.get("storage-path");
        return storage == null ? path : normalize(storage);
    }
}
