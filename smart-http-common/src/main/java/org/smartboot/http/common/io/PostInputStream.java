/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: PostInputStream.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.io;

import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/1
 */
public class PostInputStream extends BodyInputStream {
    private long remaining;

    public PostInputStream(AioSession session, long contentLength) {
        super(session);
        this.remaining = contentLength;
    }

    @Override
    public int read(byte[] data, int off, int len) throws IOException {
        if (eof) {
            return -1;
        }
        if (len == 0) {
            return 0;
        }

        ByteBuffer byteBuffer = session.readBuffer();

        if (readListener != null) {
            if (anyAreClear(state, FLAG_READY | FLAG_IS_READY_CALLED)) {
                throw new IllegalStateException();
            }
            clearFlags(FLAG_IS_READY_CALLED);
        } else if (remaining > 0 && !byteBuffer.hasRemaining()) {
            try {
                session.read();
            } catch (IOException e) {
                if (readListener != null) {
                    readListener.onError(e);
                }
                throw e;
            }

        }
        int readLength = Math.min(len, byteBuffer.remaining());
        if (remaining < readLength) {
            readLength = (int) remaining;
        }
        byteBuffer.get(data, off, readLength);
        remaining = remaining - readLength;

        if (remaining > 0) {
            if (readListener == null) {
                return readLength + read(data, off + readLength, len - readLength);
            } else {
                if (!byteBuffer.hasRemaining()) {
                    clearFlags(FLAG_READY);
                }
                return readLength;
            }
        } else {
            eof = true;
            if (readListener != null) {
                readListener.onAllDataRead();
            }
            return readLength;
        }
    }

    @Override
    public int available() {
        return Math.min((int) remaining, session.readBuffer().remaining());
    }


    public void setReadListener(ReadListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (this.readListener != null) {
            throw new IllegalStateException();
        }
        this.readListener = new ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                setFlags(FLAG_READY);
                listener.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                listener.onAllDataRead();
            }

            @Override
            public void onError(Throwable t) {
                listener.onError(t);
            }
        };
    }
}
