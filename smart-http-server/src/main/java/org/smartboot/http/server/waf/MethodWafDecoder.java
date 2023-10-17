package org.smartboot.http.server.waf;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

public class MethodWafDecoder extends AbstractWafDecoder {


    public MethodWafDecoder(HttpServerConfiguration configuration) {
        super(configuration, new WafServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws Throwable {
                response.setHttpStatus(HttpStatus.METHOD_NOT_ALLOWED);
                response.setContentLength(WafServerHandler.RESPONSE.length);
                response.getOutputStream().write(WafServerHandler.RESPONSE);
            }
        });
    }

    @Override
    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
        WafConfiguration wafConfiguration = getConfiguration().getWafConfiguration();
        if (!wafConfiguration.getAllowMethods().isEmpty() && !wafConfiguration.getAllowMethods().contains(request.getMethod())) {
            request.setServerHandler(wafServerHandler);
            return HttpRequestProtocol.BODY_READY_DECODER;
        }
        if (!wafConfiguration.getDenyMethods().isEmpty() && wafConfiguration.getDenyMethods().contains(request.getMethod())) {
            request.setServerHandler(wafServerHandler);
            return HttpRequestProtocol.BODY_READY_DECODER;
        }
        return null;
    }
}
