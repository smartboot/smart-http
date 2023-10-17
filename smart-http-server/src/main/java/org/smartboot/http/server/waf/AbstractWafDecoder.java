package org.smartboot.http.server.waf;

import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.ServerHandler;
import org.smartboot.http.server.decode.AbstractDecoder;

public abstract class AbstractWafDecoder extends AbstractDecoder {
    protected ServerHandler wafServerHandler;

    public AbstractWafDecoder(HttpServerConfiguration configuration, ServerHandler wafServerHandler) {
        super(configuration);
        this.wafServerHandler = wafServerHandler;
    }
}
