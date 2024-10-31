package org.smartboot.http.server.h2.codec;

import java.nio.ByteBuffer;

public class ContinuationFrame extends HeadersFrame {

    public ContinuationFrame(HeadersFrame prevFrame, int flags, int remaining) {
        super(prevFrame.session, prevFrame.streamId, flags, remaining);
    }


    @Override
    public boolean decode(ByteBuffer buffer) {
        return false;
    }

    @Override
    public int type() {
        return FRAME_TYPE_CONTINUATION;
    }

}
