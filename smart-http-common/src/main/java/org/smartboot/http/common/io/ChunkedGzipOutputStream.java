package org.smartboot.http.common.io;

import org.smartboot.http.common.utils.Constant;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class ChunkedGzipOutputStream extends OutputStream {
    private final OutputStream writeBuffer;

    public ChunkedGzipOutputStream(OutputStream writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedEncodingException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] start = (Integer.toHexString(len) + "\r\n").getBytes();
        writeBuffer.write(start);
        writeBuffer.write(b, off, len);
        writeBuffer.write(Constant.CRLF_BYTES);
    }
}
