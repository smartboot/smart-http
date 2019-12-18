package org.smartboot.servlet.handler;

import org.smartboot.http.utils.AntPathMatcher;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.impl.FilterChainImpl;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 匹配并执行符合当前请求的Filter
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class FilterMatchHandler extends Handler {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void handleRequest(HandlerContext handlerContext) throws Exception {
        HttpServletRequest request = handlerContext.getRequest();
        String contextPath = handlerContext.getDeploymentRuntime().getServletContext().getContextPath();
        //匹配Filter
        List<Filter> filters = new ArrayList<>();
        List<FilterMappingInfo> filterMappings = handlerContext.getDeploymentRuntime().getDeploymentInfo().getFilterMappings();
        Map<String, FilterInfo> allFilters = handlerContext.getDeploymentRuntime().getDeploymentInfo().getFilters();
        filterMappings.forEach(filterInfo -> {
            switch (filterInfo.getMappingType()) {
                case URL:
                    if (PATH_MATCHER.match(contextPath + filterInfo.getMapping(), request.getRequestURI())) {
                        filters.add(allFilters.get(filterInfo.getFilterName()).getFilter());
                    }
                    break;
                case SERVLET:
                    if (StringUtils.equals(filterInfo.getMapping(), handlerContext.getServlet().getServletConfig().getServletName())) {
                        filters.add(allFilters.get(filterInfo.getFilterName()).getFilter());
                    }
                    break;
            }
        });

        //Filter执行完毕后执行Servlet
        if (!filters.isEmpty()) {
            FilterChain filterChain = new FilterChainImpl(filters);
            filterChain.doFilter(handlerContext.getRequest(), handlerContext.getResponse());
        }
        doNext(handlerContext);
    }
}
