package org.smartboot.servlet.conf;

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
    private final List<String> mappings = new ArrayList<>();
    private final Map<String, String> initParams = new HashMap<>();
    private String servletClass;
    private String servletName;
    private int loadOnStartup;
    private Servlet servlet;

    public ServletInfo() {
    }

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
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

    public ServletInfo addInitParam(final String name, final String value) {
        initParams.put(name, value);
        return this;
    }

    public ServletInfo addMapping(final String mapping) {
        if (!mapping.startsWith("/") && !mapping.startsWith("*") && !mapping.isEmpty()) {
            //if the user adds a mapping like 'index.html' we transparently translate it to '/index.html'
            mappings.add("/" + mapping);
        } else {
            mappings.add(mapping);
        }
        return this;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }
}
