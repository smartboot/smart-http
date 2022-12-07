package org.smartboot.http.common.io;

import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.AioSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/12/6
 */
public class ChunkedInputStream extends InputStream {
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
    private boolean readFlag = true;
    private final AioSession session;
    private InputStream inputStream;
    private boolean eof = false;

    public ChunkedInputStream(AioSession session) {
        this.session = session;
    }

    @Override
    public int read() throws IOException {
        readChunkedLength();
        if (eof) {
            return -1;
        }
        int b = inputStream.read();
        if (b == -1) {
            inputStream.close();
            readFlag = true;
        }
        return b;
    }

    @Override
    public int read(byte[] data, int off, int len) throws IOException {
        readChunkedLength();
        if (eof) {
            return -1;
        }
        int i = inputStream.read(data, off, len);
        if (i == -1) {
            inputStream.close();
            readFlag = true;
        }
        return i;
    }

    private void readChunkedLength() throws IOException {
        while (readFlag) {
            inputStream = session.getInputStream();
            int b = inputStream.read();
            if (b == Constant.LF) {
                int length = Integer.parseInt(buffer.toString(), 16);
                if (length == 0) {
                    eof = true;
                    break;
                }
                inputStream.close();
                inputStream = session.getInputStream(length);
                readFlag = false;
            } else if (b != Constant.CR) {
                buffer.write(b);
            }
        }
    }
}
