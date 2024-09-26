package org.smartboot.http.server.h2;


import java.nio.ByteBuffer;

public class DataFrame extends Http2Frame {

    public static final int TYPE = 0x0;

    private static final int STATE_PAD_LENGTH = 0;
    private static final int STATE_DATA = 1;
    private static final int STATE_PADDING = 2;
    private int state = STATE_PAD_LENGTH;


    private int padLength = -1;
    private ByteBuffer dataBuffer;
    private byte[] padding = EMPTY_PADDING;


    public DataFrame(int streamId, int flags, int remaining) {
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
                dataBuffer = ByteBuffer.allocate(remaining - padLength);
                state = STATE_DATA;
            case STATE_DATA:
                int min = Math.min(buffer.remaining(), dataBuffer.remaining());
                int limit = buffer.limit();
                buffer.limit(buffer.position() + min);
                dataBuffer.put(buffer);
                buffer.limit(limit);
                remaining -= min;
                if (dataBuffer.hasRemaining()) {
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

        checkEndRemaining();
        return true;
    }

    public byte[] getPadding() {
        return padding;
    }

    @Override
    public int type() {
        return TYPE;
    }

}
