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
public class PostInputStream extends AbstractInputStream {
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
        if (remaining > 0 && !byteBuffer.hasRemaining()) {
            session.syncRead();
        }
        int readLength = Math.min(len, byteBuffer.remaining());
        if (remaining < readLength) {
            readLength = (int) remaining;
        }
        byteBuffer.get(data, off, readLength);
        remaining = remaining - readLength;

        if (remaining > 0) {
            return readLength + read(data, off + readLength, len - readLength);
        } else {
            eof = true;
            return readLength;
        }
    }

    @Override
    public int available() {
        return Math.min((int) remaining, session.readBuffer().remaining());
    }


    @Override
    public int read() throws IOException {
        if (eof) {
            return -1;
        }
        remaining--;
        eof = remaining == 0;
        return readByte();
    }
}
