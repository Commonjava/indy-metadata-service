package org.commonjava.service.metadata.model;

public interface SpecialPathMatcher
{
    boolean matches(StoreKey var1, String var2);

    String getPattern();
}
