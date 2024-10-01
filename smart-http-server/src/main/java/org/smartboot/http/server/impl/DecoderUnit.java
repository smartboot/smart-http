package org.smartboot.http.server.impl;

import org.smartboot.http.common.DecodeState;
import org.smartboot.http.server.ServerHandler;

import java.util.function.Function;

public class DecoderUnit extends DecodeState {
    private Function<String, ServerHandler<?, ?>> headerFunc;

    public DecoderUnit() {
        super(DecodeState.STATE_METHOD);
    }

    public Function<String, ServerHandler<?, ?>> getHeaderFunc() {
        return headerFunc;
    }

    public void setHeaderFunc(Function<String, ServerHandler<?, ?>> headerFunc) {
        this.headerFunc = headerFunc;
    }
}
