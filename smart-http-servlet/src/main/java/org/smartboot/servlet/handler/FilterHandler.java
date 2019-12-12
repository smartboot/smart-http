package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.impl.FilterChainImpl;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import java.util.List;

/**
 * 匹配并执行符合当前请求的Filter
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class FilterHandler implements Handler {

    private final Handler next;

    public FilterHandler(Handler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HandlerContext exchange) throws Exception {
        //匹配Filter
        List<Filter> filters = null;


        //Filter执行完毕后执行Servlet
        if (filters == null) {
            next.handleRequest(exchange);
        } else {
            FilterChain filterChain = new FilterChainImpl(next, exchange, filters);
            filterChain.doFilter(exchange.getRequest(), exchange.getResponse());
        }
    }
}
