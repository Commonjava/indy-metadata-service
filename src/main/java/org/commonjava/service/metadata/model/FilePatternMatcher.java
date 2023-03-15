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
