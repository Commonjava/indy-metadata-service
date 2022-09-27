package org.commonjava.service.metadata.model;

public class PathPatternMatcher implements SpecialPathMatcher {
    private final String pattern;

    public PathPatternMatcher(String pattern) {
        this.pattern = pattern;
    }

    public boolean matches(StoreKey key, String path) {
        return path != null && path.matches(this.pattern);
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PathPatternMatcher)) {
            return false;
        } else {
            PathPatternMatcher that = (PathPatternMatcher)o;
            return this.pattern.equals(that.pattern);
        }
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    public String toString() {
        return "PathPatternMatcher{" + this.pattern + '}';
    }
}
