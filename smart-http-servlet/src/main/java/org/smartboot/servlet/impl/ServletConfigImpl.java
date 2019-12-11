package org.smartboot.servlet.impl;

import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.util.IteratorEnumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class ServletConfigImpl implements ServletConfig {

    private final ServletInfo servletInfo;
    private final ServletContext servletContext;

    public ServletConfigImpl(final ServletInfo servletInfo, final ServletContext servletContext) {
        this.servletInfo = servletInfo;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servletInfo.getName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(final String name) {
        return servletInfo.getInitParams().get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return new IteratorEnumeration<>(servletInfo.getInitParams().keySet().iterator());
    }
}