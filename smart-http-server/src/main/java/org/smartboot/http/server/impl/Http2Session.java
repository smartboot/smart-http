package org.smartboot.http.server.impl;

import org.smartboot.http.common.io.BodyInputStream;
import org.smartboot.http.common.io.ReadListener;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.multipart.MultipartConfig;
import org.smartboot.http.common.multipart.Part;
import org.smartboot.http.server.h2.codec.Http2Frame;
import org.smartboot.http.server.h2.codec.SettingsFrame;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

public class Http2Session extends HttpRequestImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(Http2Session.class);
    public static final int STATE_FIRST_REQUEST = 0;
    public static final int STATE_PREFACE = 1;
    public static final int STATE_PREFACE_SM = 1 << 1;
    public static final int STATE_FRAME_HEAD = 1 << 2;
    public static final int STATE_FRAME_PAYLOAD = 1 << 3;

    private final SettingsFrame settings = new SettingsFrame(0, true) {
        @Override
        public boolean decode(ByteBuffer buffer) {
            throw new IllegalStateException();
        }

        @Override
        public void writeTo(WriteBuffer writeBuffer) throws IOException {
            throw new IllegalStateException();
        }
    };
    private int streamId;
    private boolean prefaced;
    //    private final Http2ResponseImpl response;
    private Http2Frame currentFrame;
    private int state;

    public Http2Session(Request request) {
        super(request);
//        this.response = new Http2ResponseImpl(this);
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
        return new BodyInputStream(request.getAioSession()) {
            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }
        };
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
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    /**
     * 更新服务端Settings配置
     */
    public void updateSettings(SettingsFrame settingsFrame) {
        settings.setEnablePush(settingsFrame.getEnablePush());
        settings.setHeaderTableSize(settingsFrame.getHeaderTableSize());
        settings.setInitialWindowSize(settingsFrame.getInitialWindowSize());
        settings.setMaxConcurrentStreams(settingsFrame.getMaxConcurrentStreams());
        settings.setMaxFrameSize(settingsFrame.getMaxFrameSize());
        settings.setMaxHeaderListSize(settingsFrame.getMaxHeaderListSize());
        LOGGER.info("updateSettings:" + settings);
    }
}
