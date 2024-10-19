package org.smartboot.http.server.h2.codec;

import java.nio.ByteBuffer;

public class ContinuationFrame extends Http2Frame {

    public static final int TYPE = 0x9;

    public ContinuationFrame(int streamId, int flags, int remaining) {
        super(streamId, flags, remaining);
    }


    @Override
    public boolean decode(ByteBuffer buffer) {
        return false;
    }

    @Override
    public int type() {
        return TYPE;
    }

}
