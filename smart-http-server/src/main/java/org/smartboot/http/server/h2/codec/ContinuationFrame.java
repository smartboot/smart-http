package org.smartboot.http.server.h2.codec;

import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ContinuationFrame extends HeadersFrame {

    public ContinuationFrame(HeadersFrame prevFrame, int flags, int remaining) {
        super(prevFrame.streamId, flags, remaining);
    }


    @Override
    public boolean decode(ByteBuffer buffer) {
        return false;
    }

    @Override
    public void writeTo(WriteBuffer writeBuffer) throws IOException {
        super.writeTo(writeBuffer);
    }

    @Override
    public int type() {
        return FRAME_TYPE_CONTINUATION;
    }

}
