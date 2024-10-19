package org.smartboot.http.server.h2.codec;

import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;


public abstract class Http2Frame {
    protected static final byte[] EMPTY_PADDING = new byte[0];
    public static final int FLAG_END_STREAM = 0x1;
    public static final int FLAG_END_HEADERS = 0x4;
    public static final int FLAG_PADDED = 0x8;
    public static final int FLAG_PRIORITY = 0x20;
    protected final int streamId;
    protected final int flags;
    protected int remaining;

    protected static final int STATE_PAD_LENGTH = 0;
    protected static final int STATE_STREAM_DEPENDENCY = 1;
    protected static final int STATE_STREAM_ID = 1;
    protected static final int STATE_FRAGMENT = 2;
    protected static final int STATE_PADDING = 3;
    /**
     * 解码阶段
     */
    protected int state = STATE_PAD_LENGTH;

    public Http2Frame(int streamId, int flags, int remaining) {
        this.streamId = streamId;
        this.flags = flags;
        this.remaining = remaining;
    }

    public int streamId() {
        return streamId;
    }

    public int getFlags() {
        return flags;
    }

    public final boolean getFlag(int flag) {
        return hasFlag(flags, flag);
    }

    protected final boolean hasFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }

    protected boolean finishDecode() {
        if (remaining < 0) {
            throw new IllegalStateException();
        }
        return remaining == 0;
    }

    protected void checkEndRemaining() {
        if (remaining != 0) {
            throw new IllegalStateException();
        }
    }

    /**
     * Decode the frame
     *
     * @param buffer
     * @return true if the frame is complete
     */
    public abstract boolean decode(ByteBuffer buffer);

    public void writeTo(WriteBuffer writeBuffer) throws IOException {
        throw new IllegalStateException();
    }

    public abstract int type();

}
