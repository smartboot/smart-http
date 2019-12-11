package org.smartboot.servlet.impl;

import org.smartboot.servlet.HttpServerExchange;
import org.smartboot.servlet.handler.Handler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class FilterChainImpl implements FilterChain {
    final List<Filter> filters;
    int location = 0;
    private Handler handler;

    private HttpServerExchange exchange;


    public FilterChainImpl(Handler handler, HttpServerExchange exchange, List<Filter> filters) {
        this.filters = filters;
        this.handler = handler;
        this.exchange = exchange;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        int index = location++;
        if (index >= filters.size()) {
            try {
                handler.handleRequest(exchange);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            filters.get(index).doFilter(request, response, this);
        }
    }
}
