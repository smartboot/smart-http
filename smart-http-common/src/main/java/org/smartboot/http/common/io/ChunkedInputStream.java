package org.smartboot.http.common.io;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
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
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(8);
    private boolean readFlag = true;
    private final AioSession session;
    private InputStream inputStream;
    private boolean eof = false;

    public ChunkedInputStream(AioSession session) {
        this.session = session;
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException("unsafe operation");
//        readChunkedLength();
//        if (eof) {
//            return -1;
//        }
//        int b = inputStream.read();
//        if (b == -1) {
//            inputStream.close();
//            inputStream = session.getInputStream();
//            readCrlf();
//            readFlag = true;
//            return read();
//        }
//        return b;
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
            inputStream = session.getInputStream();
            readCrlf();
            readFlag = true;
            return read(data, off, len);
        }
        return i;
    }

    private void readChunkedLength() throws IOException {
        while (readFlag) {
            inputStream = session.getInputStream();
            int b = inputStream.read();
            if (b == -1) {
                throw new IOException("inputStream is closed");
            }
            if (b == Constant.LF) {
                int length = Integer.parseInt(buffer.toString(), 16);
                buffer.reset();
                if (length == 0) {
                    eof = true;
                    readCrlf();
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

    private void readCrlf() throws IOException {
        if (inputStream.read() != Constant.CR) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }
        if (inputStream.read() != Constant.LF) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }
}
