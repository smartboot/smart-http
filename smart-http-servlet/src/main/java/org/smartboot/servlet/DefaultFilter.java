package org.smartboot.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DefaultFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
        Enumeration<String> enumeration = config.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            LOGGER.info("filter parameter name:{} ,value:{}", name, config.getInitParameter(name));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {

    }
}
