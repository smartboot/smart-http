package org.smartboot.http.server.waf;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

public class MethodWafDecoder extends AbstractWafDecoder {


    public MethodWafDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
        WafConfiguration wafConfiguration = getConfiguration().getWafConfiguration();
        if (!wafConfiguration.getAllowMethods().isEmpty() && !wafConfiguration.getAllowMethods().contains(request.getMethod())) {
            throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, WafConfiguration.DESC);
        }
        if (!wafConfiguration.getDenyMethods().isEmpty() && wafConfiguration.getDenyMethods().contains(request.getMethod())) {
            throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, WafConfiguration.DESC);
        }
        return null;
    }
}
