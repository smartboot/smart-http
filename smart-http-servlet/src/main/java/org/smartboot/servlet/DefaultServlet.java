package org.smartboot.servlet;


import org.smartboot.http.logging.Logger;
import org.smartboot.http.logging.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Enumeration;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DefaultServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Enumeration<String> enumeration = config.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            LOGGER.info("servlet parameter name:" + name + " ,value:" + config.getInitParameter(name));
        }

    }
}
