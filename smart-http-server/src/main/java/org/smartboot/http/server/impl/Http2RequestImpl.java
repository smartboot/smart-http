package org.smartboot.http.server.impl;

import org.smartboot.http.common.io.BodyInputStream;
import org.smartboot.http.server.h2.Http2Frame;

import java.io.IOException;

public class Http2RequestImpl extends AbstractRequest {
    private boolean prefaced;
    private final Http2ResponseImpl response;
    private Http2Frame currentFrame;

    public Http2RequestImpl(Request request) {
        init(request);
        this.response = new Http2ResponseImpl(this);
    }

    @Override
    public AbstractResponse getResponse() {
        return response;
    }

    @Override
    public void reset() {

    }

    public boolean isPrefaced() {
        return prefaced;
    }

    public void setPrefaced(boolean prefaced) {
        this.prefaced = prefaced;
    }

    @Override
    public BodyInputStream getInputStream() throws IOException {
        return null;
    }

    public Http2Frame getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(Http2Frame currentFrame) {
        this.currentFrame = currentFrame;
    }
}
