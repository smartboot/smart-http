package org.smartboot.http.common.io;

import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class BodyInputStream extends InputStream {
    protected final AioSession session;
    protected boolean eof = false;
    protected ReadListener readListener;
    protected volatile int state;
    protected static final int FLAG_READY = 1;
    protected static final int FLAG_FINISHED = 1 << 1;
    protected static final int FLAG_CLOSED = 1 << 2;
    protected static final int FLAG_IS_READY_CALLED = 1 << 3;
    protected static final int FLAG_CHUNKED_TRAILER = 1 << 4;
    protected static final int FLAG_EXPECT_CR_LF = 1 << 5;
    //需要解析chunked长度
    protected static final int FLAG_READ_CHUNKED_LENGTH = 1 << 6;
    protected static final AtomicIntegerFieldUpdater<BodyInputStream> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(BodyInputStream.class, "state");

    public BodyInputStream(AioSession session) {
        this.session = session;
    }


    @Override
    public void close() throws IOException {
        eof = true;
    }

    @Override
    public final int read() throws IOException {
        byte[] b = new byte[1];
        int read = read(b);
        if (read == -1) {
            return -1;
        }
        return b[0] & 0xff;
    }

    /**
     * listener#onAllDataRead方法需要触发futuren.complete
     *
     * @param listener
     */
    public abstract void setReadListener(ReadListener listener);

    public ReadListener getReadListener() {
        return readListener;
    }

    public boolean isFinished() {
        return anyAreSet(state, FLAG_FINISHED);
    }

    public boolean isReady() {
        if (readListener == null) {
            return true;
        }
//        boolean finished = anyAreSet(state, FLAG_FINISHED);
//        if (finished) {
//            if (anyAreClear(state, FLAG_ON_DATA_READ_CALLED)) {
//                if (allAreClear(state, FLAG_BEING_INVOKED_IN_IO_THREAD)) {
//                    setFlags(FLAG_ON_DATA_READ_CALLED);
//                } else {
//                    setFlags(FLAG_CALL_ON_ALL_DATA_READ);
//                }
//            }
//        }
        return anyAreSet(state, FLAG_READY) && session.readBuffer().hasRemaining();
    }

    protected static boolean anyAreClear(int var, int flags) {
        return (var & flags) != flags;
    }

    protected void clearFlags(int flags) {
        int old;
        do {
            old = state;
        } while (!stateUpdater.compareAndSet(this, old, old & ~flags));
    }

    protected void setFlags(int flags) {
        int old;
        do {
            old = state;
        } while (!stateUpdater.compareAndSet(this, old, old | flags));
    }

    protected boolean anyAreSet(int var, int flags) {
        return (var & flags) != 0;
    }

    protected boolean allAreClear(int var, int flags) {
        return (var & flags) == 0;
    }
}
