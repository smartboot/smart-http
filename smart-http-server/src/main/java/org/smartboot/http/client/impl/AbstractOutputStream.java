/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractOutputStream.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.BufferOutputStream;
import org.smartboot.http.enums.HeaderNameEnum;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
abstract class AbstractOutputStream extends BufferOutputStream {
    private static final byte[] CHUNKED_END_BYTES = "0\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
    protected static byte[] date;

    protected final WriteBuffer writeBuffer;
    protected final AbstractRequest request;
    protected boolean committed = false;
    /**
     * 当前流是否完结
     */
    protected boolean closed = false;
    protected boolean chunked = false;

    public AbstractOutputStream(AbstractRequest request, WriteBuffer writeBuffer) {
        this.request = request;
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

    @Override
    public final void write(VirtualBuffer virtualBuffer) throws IOException {
        writeHead();
        if (chunked) {
            byte[] start = getBytes(Integer.toHexString(virtualBuffer.buffer().remaining()) + "\r\n");
            writeBuffer.write(start);
            writeBuffer.write(virtualBuffer);
            writeBuffer.write(Constant.CRLF);
        } else {
            writeBuffer.write(virtualBuffer);
        }
    }

    /**
     * 输出Http消息头
     *
     * @throws IOException
     */
    abstract void writeHead() throws IOException;


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
            writeBuffer.write(CHUNKED_END_BYTES);
        }
        closed = true;
    }

    final protected byte[] getHeaderNameBytes(String name) {
        HeaderNameEnum headerNameEnum = HttpHeaderConstant.HEADER_NAME_ENUM_MAP.get(name);
        if (headerNameEnum != null) {
            return headerNameEnum.getBytesWithColon();
        }
        byte[] extBytes = HttpHeaderConstant.HEADER_NAME_EXT_MAP.get(name);
        if (extBytes == null) {
            synchronized (name) {
                extBytes = getBytes("\r\n" + name + ":");
                HttpHeaderConstant.HEADER_NAME_EXT_MAP.put(name, extBytes);
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
}
