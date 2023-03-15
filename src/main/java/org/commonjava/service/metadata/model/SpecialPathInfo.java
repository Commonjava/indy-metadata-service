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
package org.commonjava.service.metadata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SpecialPathInfo {

    @JsonIgnore
    private SpecialPathMatcher matcher;

    private String pattern;
    private boolean retrievable = true;
    private boolean publishable = true;
    private boolean listable = true;
    private boolean decoratable = true;
    private boolean storable = true;
    private boolean deletable = true;
    private boolean metadata = false;
    private boolean mergable = false;
    private boolean cachable = true;

    public static Builder from(SpecialPathMatcher matcher) {
        return new Builder(matcher);
    }

    protected SpecialPathInfo(SpecialPathMatcher matcher, boolean retrievable, boolean publishable, boolean listable, boolean decoratable, boolean storable, boolean deletable, boolean metadata, boolean mergable, boolean cachable) {
        this.matcher = matcher;
        this.retrievable = retrievable;
        this.publishable = publishable;
        this.listable = listable;
        this.decoratable = decoratable;
        this.storable = storable;
        this.deletable = deletable;
        this.metadata = metadata;
        this.mergable = mergable;
        this.cachable = cachable;
    }

    protected SpecialPathInfo(SpecialPathMatcher matcher) {
        this.matcher = matcher;
    }

    public SpecialPathMatcher getMatcher() {
        return this.matcher;
    }

    protected void setMatcher(SpecialPathMatcher matcher) {
        this.matcher = matcher;
    }

    public boolean isRetrievable() {
        return this.retrievable;
    }

    protected void setRetrievable(boolean retrievable) {
        this.retrievable = retrievable;
    }

    public boolean isPublishable() {
        return this.publishable;
    }

    protected void setPublishable(boolean publishable) {
        this.publishable = publishable;
    }

    public boolean isListable() {
        return this.listable;
    }

    protected void setListable(boolean listable) {
        this.listable = listable;
    }

    public boolean isDecoratable() {
        return this.decoratable;
    }

    protected void setDecoratable(boolean decoratable) {
        this.decoratable = decoratable;
    }

    protected void setStorable(boolean storable) {
        this.storable = storable;
    }

    public boolean isStorable() {
        return this.storable;
    }

    public boolean isDeletable() {
        return this.deletable;
    }

    protected void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isMetadata() {
        return this.metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public boolean isMergable() {
        return this.mergable;
    }

    protected void setMergable(boolean mergable) {
        this.mergable = mergable;
    }

    public boolean isCachable() {
        return this.cachable;
    }

    public void setCachable(boolean cachable) {
        this.cachable = cachable;
    }

    public String getPattern() {
        return matcher.getPattern();
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SpecialPathInfo)) {
            return false;
        } else {
            SpecialPathInfo that = (SpecialPathInfo)o;
            return this.getMatcher().equals(that.getMatcher());
        }
    }

    public int hashCode() {
        return this.getMatcher().hashCode();
    }

    public static class Builder {
        private SpecialPathInfo info;

        protected Builder(SpecialPathMatcher matcher) {
            this.info = new SpecialPathInfo(matcher);
        }

        public SpecialPathInfo build() {
            SpecialPathInfo result = this.info;
            this.info = new SpecialPathInfo(result.getMatcher());
            return result;
        }

        public boolean isRetrievable() {
            return this.info.isRetrievable();
        }

        public boolean isDecoratable() {
            return this.info.isDecoratable();
        }

        public boolean isListable() {
            return this.info.isListable();
        }

        public boolean isPublishable() {
            return this.info.isPublishable();
        }

        public SpecialPathMatcher getMatcher() {
            return this.info.getMatcher();
        }

        public Builder setRetrievable(boolean retrievable) {
            this.info.setRetrievable(retrievable);
            return this;
        }

        public Builder setListable(boolean listable) {
            this.info.setListable(listable);
            return this;
        }

        public Builder setDecoratable(boolean decoratable) {
            this.info.setDecoratable(decoratable);
            return this;
        }

        public Builder setPublishable(boolean publishable) {
            this.info.setPublishable(publishable);
            return this;
        }

        public Builder setStorable(boolean storable) {
            this.info.setStorable(storable);
            return this;
        }

        public boolean isStorable() {
            return this.info.isStorable();
        }

        public Builder setDeletable(boolean deletable) {
            this.info.setDeletable(deletable);
            return this;
        }

        public boolean isDeletable() {
            return this.info.isDeletable();
        }

        public Builder setMetadata(boolean metadata) {
            this.info.setMetadata(metadata);
            return this;
        }

        public boolean isMetadata() {
            return this.info.isMetadata();
        }

        public Builder setMergable(boolean mergable) {
            this.info.setMergable(mergable);
            return this;
        }

        public boolean isMergable() {
            return this.info.mergable;
        }

        public Builder setCachable(boolean cachable) {
            this.info.setCachable(cachable);
            return this;
        }

        public boolean isCachable() {
            return this.info.isCachable();
        }
    }
}
