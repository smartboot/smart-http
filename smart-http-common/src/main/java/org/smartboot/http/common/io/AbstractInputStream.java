package org.smartboot.http.common.io;

import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class AbstractInputStream extends InputStream {
    protected boolean readFlag = true;
    protected final AioSession session;
    protected boolean eof = false;

    public AbstractInputStream(AioSession session) {
        this.session = session;
    }

    protected byte readByte() throws IOException {
        ByteBuffer byteBuffer = session.readBuffer();
        if (!byteBuffer.hasRemaining()) {
            int i = session.read();
            if (i == -1) {
                throw new IOException("inputStream is closed");
            }
        }
        return byteBuffer.get();
    }

    @Override
    public void close() throws IOException {
        eof = true;
    }
}
