package org.smartboot.http.server.h2;

import java.nio.ByteBuffer;

public class WindowUpdateFrame extends Http2Frame {

    private int windowUpdate;

    public static final int TYPE = 0x8;

    public WindowUpdateFrame(int streamId, int flags, int remaining) {
        super(streamId, flags, remaining);
    }

    @Override
    public boolean decode(ByteBuffer buffer) {
        if (buffer.remaining() < 4) {
            return false;
        }
        windowUpdate = buffer.getInt();
        remaining = 0;
        return true;
    }

    @Override
    public int type() {
        return TYPE;
    }

    public int getUpdate() {
        return this.windowUpdate;
    }

}
