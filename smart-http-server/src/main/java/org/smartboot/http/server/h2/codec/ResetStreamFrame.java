package org.smartboot.http.server.h2.codec;

import java.nio.ByteBuffer;

public class ResetStreamFrame extends Http2Frame {

    public static final int TYPE = 0x3;

    private int errorCode;

    public ResetStreamFrame(int streamid, int errorCode) {
        super(streamid, 0, errorCode);
    }

    @Override
    public boolean decode(ByteBuffer buffer) {
        if (finishDecode()) {
            return true;
        }
        if (buffer.remaining() < 4) {
            return false;
        }
        errorCode = buffer.getInt();
        remaining -= 4;
        return true;
    }

    @Override
    public int type() {
        return TYPE;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
