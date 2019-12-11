package org.smartboot.servlet;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletInfo {
    private final Class<? extends Servlet> servletClass;
    private final String name;
    private final List<String> mappings = new ArrayList<>();
    private final Map<String, String> initParams = new HashMap<>();
    private Servlet servlet;

    public ServletInfo(Class<? extends Servlet> servletClass, String name) {
        this.servletClass = servletClass;
        this.name = name;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    public List<String> getMappings() {
        return Collections.unmodifiableList(mappings);
    }

    public ServletInfo addMapping(final String mapping) {
        if(!mapping.startsWith("/") && !mapping.startsWith("*") && !mapping.isEmpty()) {
            //if the user adds a mapping like 'index.html' we transparently translate it to '/index.html'
            mappings.add("/" + mapping);
        } else {
            mappings.add(mapping);
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }

    public Class<? extends Servlet> getServletClass() {
        return servletClass;
    }
}
