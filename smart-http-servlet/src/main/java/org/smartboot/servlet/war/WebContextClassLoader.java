package org.smartboot.servlet.war;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class WebContextClassLoader {
    private String location;

    private ClassLoader classLoader;

    public WebContextClassLoader(String location) {
        this.location = location;
    }

    public ClassLoader getClassLoader() throws MalformedURLException {
        if (classLoader != null) {
            return classLoader;
        }
        List<URL> list = new ArrayList<>();
        File libDir = new File(location, "WEB-INF" + File.separator + "lib/");
        for (File file : libDir.listFiles()) {
            list.add(file.toURI().toURL());
        }

        File classDir = new File(location, "WEB-INF" + File.separator + "classes/");
        list.add(classDir.toURI().toURL());
        classLoader = new URLClassLoader(list.toArray(new URL[list.size()]), Thread.currentThread().getContextClassLoader());
        return classLoader;
    }
}
