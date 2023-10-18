package org.smartboot.http.server.waf;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.CollectionUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

public class UriWafDecoder extends AbstractWafDecoder {


    public UriWafDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
        WafConfiguration wafConfiguration = getConfiguration().getWafConfiguration();
        if (request.getUri().equals("/") || CollectionUtils.isEmpty(wafConfiguration.getAllowUriPrefixes()) && CollectionUtils.isEmpty(wafConfiguration.getAllowUriSuffixes())) {
            return null;
        }
        for (String prefix : wafConfiguration.getAllowUriPrefixes()) {
            if (request.getUri().startsWith(prefix)) {
                return null;
            }
        }
        for (String suffix : wafConfiguration.getAllowUriSuffixes()) {
            if (request.getUri().endsWith(suffix)) {
                return null;
            }
        }
        throw new HttpException(HttpStatus.BAD_REQUEST, WafConfiguration.DESC);
    }
}
