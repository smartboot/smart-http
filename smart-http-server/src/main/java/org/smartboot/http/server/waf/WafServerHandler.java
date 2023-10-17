package org.smartboot.http.server.waf;

import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

public class WafServerHandler extends HttpServerHandler {
    public static final byte[] RESPONSE = "Mysterious Power from the East Is Protecting This Area".getBytes();

    @Override
    public boolean onBodyStream(ByteBuffer buffer, Request request) {
        return true;
    }
}
