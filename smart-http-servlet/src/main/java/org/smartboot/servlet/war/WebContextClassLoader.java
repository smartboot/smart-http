package org.smartboot.servlet.war;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class WebContextClassLoader {
    List<String> aa = Arrays.asList(
            "com.yangt.log.filter.YtLoggerEventFilter"
            , "com.yt.trade.biz.util.LoggerStartupListener"
            ,"org.slf4j.Logger","org.slf4j.LoggerFactory"
    );
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
        classLoader = new URLClassLoader(list.toArray(new URL[list.size()]), Thread.currentThread().getContextClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (aa.contains(name)) {
                    System.out.println("class:" + name);
                }
                return super.findClass(name);
            }
        };
        return classLoader;
    }
}
