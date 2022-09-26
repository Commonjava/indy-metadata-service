package org.commonjava.service.metadata.model;

import java.io.File;

public class FilePatternMatcher implements SpecialPathMatcher {
    private final String pattern;

    public FilePatternMatcher(String pattern) {
        this.pattern = pattern;
    }

    public boolean matches(StoreKey key, String path) {
        return path != null && (new File(path)).getName().matches(this.pattern);
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FilePatternMatcher)) {
            return false;
        } else {
            FilePatternMatcher that = (FilePatternMatcher)o;
            return this.pattern.equals(that.pattern);
        }
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    public String toString() {
        return "FilePatternMatcher{" + this.pattern + '}';
    }
}
