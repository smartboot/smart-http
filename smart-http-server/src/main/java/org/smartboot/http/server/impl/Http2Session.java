package org.smartboot.http.server.impl;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.server.h2.codec.Http2Frame;
import org.smartboot.http.server.h2.codec.SettingsFrame;
import org.smartboot.http.server.h2.hpack.Decoder;
import org.smartboot.http.server.h2.hpack.Encoder;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class Http2Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(Http2Session.class);
    public static final int STATE_FIRST_REQUEST = 0;
    public static final int STATE_PREFACE = 1;
    public static final int STATE_PREFACE_SM = 1 << 1;
    public static final int STATE_FRAME_HEAD = 1 << 2;
    public static final int STATE_FRAME_PAYLOAD = 1 << 3;
    private final ConcurrentHashMap<Integer, Http2RequestImpl> streams = new ConcurrentHashMap<>();
    private Decoder hpackDecoder = new Decoder(4096);
    private Encoder hpackEncoder = new Encoder(4096);

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
    private final Request request;

    public Http2Session(Request request) {
        this.request = request;
//        this.response = new Http2ResponseImpl(this);
    }

    public Http2RequestImpl getStream(int streamId) {
        return streams.computeIfAbsent(streamId, k -> new Http2RequestImpl(streamId, request));
    }


    public boolean isPrefaced() {
        return prefaced;
    }

    public void setPrefaced(boolean prefaced) {
        this.prefaced = prefaced;
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

    public Decoder getHpackDecoder() {
        return hpackDecoder;
    }

    public Encoder getHpackEncoder() {
        return hpackEncoder;
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
