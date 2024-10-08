package org.smartboot.http.server.impl;

import org.smartboot.http.common.io.BodyInputStream;
import org.smartboot.http.common.multipart.MultipartConfig;
import org.smartboot.http.common.multipart.Part;
import org.smartboot.http.server.h2.Http2Frame;

import java.io.IOException;
import java.util.Collection;

public class Http2RequestImpl extends AbstractRequest {
    public static final int STATE_FIRST_REQUEST = 0;
    public static final int STATE_PREFACE = 1;
    public static final int STATE_FRAME_HEAD = 1 << 1;
    public static final int STATE_FRAME_PAYLOAD = 1 << 2;
    private boolean prefaced;
    private final Http2ResponseImpl response;
    private Http2Frame currentFrame;
    private int state;

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

    @Override
    public Collection<Part> getParts(MultipartConfig configElement) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Http2Frame getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(Http2Frame currentFrame) {
        this.currentFrame = currentFrame;
    }

    public int getState() {
        throw new UnsupportedOperationException();
    }

    public void setState(int state) {
        throw new UnsupportedOperationException();
    }
}
