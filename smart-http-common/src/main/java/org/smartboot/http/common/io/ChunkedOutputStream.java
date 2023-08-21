package org.smartboot.http.common.io;

import org.smartboot.http.common.utils.Constant;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class ChunkedOutputStream extends OutputStream {
    private final OutputStream writeBuffer;

    public ChunkedOutputStream(OutputStream writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    @Override
    public final void write(int b) throws IOException {
        throw new UnsupportedEncodingException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] start = (Integer.toHexString(len) + "\r\n").getBytes();
        writeBuffer.write(start);
        writeBuffer.write(b, off, len);
        writeBuffer.write(Constant.CRLF_BYTES);
    }

    @Override
    public void close() throws IOException {
        writeBuffer.write(Constant.CHUNKED_END_BYTES);
    }
}
