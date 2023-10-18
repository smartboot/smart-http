package org.smartboot.http.server.waf;

import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.AbstractDecoder;

public abstract class AbstractWafDecoder extends AbstractDecoder {

    public AbstractWafDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }
}
