package org.smartboot.http.server.h2.codec;

import java.nio.ByteBuffer;

public class ContinuationFrame extends Http2Frame {

    public ContinuationFrame(int streamId, int flags, int remaining) {
        super(streamId, flags, remaining);
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
