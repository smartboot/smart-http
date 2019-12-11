/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.smartboot.servlet.conf;

import javax.servlet.Filter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class FilterInfo {

    private final Class<? extends Filter> filterClass;
    private final String name;
    private final Map<String, String> initParams = new HashMap<>();
    private Filter filter;

    public FilterInfo(Class<? extends Filter> filterClass, String name) {
        this.filterClass = filterClass;
        this.name = name;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    public String getName() {
        return name;
    }


    public FilterInfo addInitParam(final String name, final String value) {
        initParams.put(name, value);
        return this;
    }

    public Map<String, String> getInitParams() {
        return Collections.unmodifiableMap(initParams);
    }


    @Override
    public String toString() {
        return "FilterInfo{" +
                "filterClass=" + filterClass +
                ", name='" + name + '\'' +
                '}';
    }
}
