package org.smartboot.http.server.waf;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.CollectionUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.impl.Request;

public class WAF {
    public static void methodCheck(HttpServerConfiguration configuration, Request request) {
        WafConfiguration wafConfiguration = configuration.getWafConfiguration();
        if (!wafConfiguration.getAllowMethods().isEmpty() && !wafConfiguration.getAllowMethods().contains(request.getMethod())) {
            throw new WafException(HttpStatus.METHOD_NOT_ALLOWED, WafConfiguration.DESC);
        }
        if (!wafConfiguration.getDenyMethods().isEmpty() && wafConfiguration.getDenyMethods().contains(request.getMethod())) {
            throw new WafException(HttpStatus.METHOD_NOT_ALLOWED, WafConfiguration.DESC);
        }
    }

    public static void checkUri(HttpServerConfiguration configuration, Request request) {
        WafConfiguration wafConfiguration = configuration.getWafConfiguration();
        if (request.getUri().equals("/") || CollectionUtils.isEmpty(wafConfiguration.getAllowUriPrefixes()) && CollectionUtils.isEmpty(wafConfiguration.getAllowUriSuffixes())) {
            return;
        }
        for (String prefix : wafConfiguration.getAllowUriPrefixes()) {
            if (request.getUri().startsWith(prefix)) {
                return;
            }
        }
        for (String suffix : wafConfiguration.getAllowUriSuffixes()) {
            if (request.getUri().endsWith(suffix)) {
                return;
            }
        }
        throw new WafException(HttpStatus.BAD_REQUEST, WafConfiguration.DESC);
    }
}
