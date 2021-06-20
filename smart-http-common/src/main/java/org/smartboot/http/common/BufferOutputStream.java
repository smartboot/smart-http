/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BufferOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/7
 */
public abstract class BufferOutputStream extends OutputStream implements Reset {
    private static final Map<String, byte[]> HEADER_NAME_EXT_MAP = new ConcurrentHashMap<>();
    protected final WriteBuffer writeBuffer;
    protected boolean committed = false;
    protected boolean chunked = false;
    /**
     * 当前流是否完结
     */
    private boolean closed = false;

    public BufferOutputStream(WriteBuffer writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    @Override
    public final void write(int b) {
        throw new UnsupportedOperationException();
    }

    /**
     * 输出Http响应
     *
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    public final void write(byte b[], int off, int len) throws IOException {
        writeHead();
        check();
        if (chunked) {
            byte[] start = getBytes(Integer.toHexString(len) + "\r\n");
            writeBuffer.write(start);
            writeBuffer.write(b, off, len);
            writeBuffer.write(Constant.CRLF);
        } else {
            writeBuffer.write(b, off, len);
        }
    }

    public final void write(ByteBuffer buffer) throws IOException {
        write(VirtualBuffer.wrap(buffer));
    }

    public final void write(VirtualBuffer virtualBuffer) throws IOException {
        writeHead();
        check();
        if (chunked) {
            byte[] start = getBytes(Integer.toHexString(virtualBuffer.buffer().remaining()) + "\r\n");
            writeBuffer.write(start);
            writeBuffer.write(virtualBuffer);
            writeBuffer.write(Constant.CRLF);
        } else {
            writeBuffer.write(virtualBuffer);
        }
    }

    @Override
    public final void flush() throws IOException {
        writeHead();
        writeBuffer.flush();
    }

    @Override
    public final void close() throws IOException {
        if (closed) {
            throw new IOException("outputStream has already closed");
        }
        writeHead();

        if (chunked) {
            writeBuffer.write(Constant.CHUNKED_END_BYTES);
        }
        closed = true;
    }

    protected final byte[] getHeaderNameBytes(String name) {
        HeaderNameEnum headerNameEnum = HeaderNameEnum.HEADER_NAME_ENUM_MAP.get(name);
        if (headerNameEnum != null) {
            return headerNameEnum.getBytesWithColon();
        }
        byte[] extBytes = HEADER_NAME_EXT_MAP.get(name);
        if (extBytes == null) {
            synchronized (name) {
                extBytes = getBytes("\r\n" + name + ":");
                HEADER_NAME_EXT_MAP.put(name, extBytes);
            }
        }
        return extBytes;
    }

    protected final byte[] getBytes(String str) {
        return str.getBytes(StandardCharsets.US_ASCII);
    }

    public final boolean isClosed() {
        return closed;
    }

    public final void reset() {
        committed = closed = chunked = false;
    }


    protected abstract void writeHead() throws IOException;

    protected abstract void check();

}
