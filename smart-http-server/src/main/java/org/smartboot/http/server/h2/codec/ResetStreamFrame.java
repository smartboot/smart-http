package org.smartboot.http.server.h2.codec;

import java.nio.ByteBuffer;

public class ResetStreamFrame extends Http2Frame {


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
        return FRAME_TYPE_RST_STREAM;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
