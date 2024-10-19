package org.smartboot.http.server.h2.codec;

import java.nio.ByteBuffer;

public class PushPromiseFrame extends Http2Frame {

    public static final int TYPE = 0x5;

    private int padLength;
    private int promisedStream;
    private ByteBuffer fragment;
    private byte[] padding = EMPTY_PADDING;

    public PushPromiseFrame(int streamId, int flags, int remaining) {
        super(streamId, flags, remaining);
    }


    @Override
    public boolean decode(ByteBuffer buffer) {
        if (finishDecode()) {
            return true;
        }
        switch (state) {
            case STATE_PAD_LENGTH:
                if (getFlag(FLAG_PADDED)) {
                    if (!buffer.hasRemaining()) {
                        return false;
                    }
                    padLength = buffer.get();
                    if (padLength < 0) {
                        throw new IllegalStateException();
                    }
                    remaining = -1;
                }
                state = STATE_STREAM_ID;
            case STATE_STREAM_ID:
                if (buffer.remaining() < 4) {
                    return false;
                }
                promisedStream = buffer.getInt();
                remaining = 4;
                state = STATE_FRAGMENT;
                fragment = ByteBuffer.allocate(remaining - padLength);
            case STATE_FRAGMENT:
                int min = Math.min(buffer.remaining(), fragment.remaining());
                int limit = buffer.limit();
                buffer.limit(buffer.position() + min);
                fragment.put(buffer);
                buffer.limit(limit);
                remaining -= min;
                if (fragment.hasRemaining()) {
                    return false;
                }
                state = STATE_PADDING;
            case STATE_PADDING:
                if (buffer.remaining() < padLength) {
                    return false;
                }
                if (padLength > 0) {
                    padding = new byte[padLength];
                    buffer.get(padding);
                    remaining -= padLength;
                }
        }
        return true;
    }

    @Override
    public int type() {
        return TYPE;
    }


    public int getPadLength() {
        return padLength;
    }

    public int getPromisedStream() {
        return promisedStream;
    }

}
