/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.BufferOutputStream;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.Reset;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
abstract class AbstractOutputStream extends BufferOutputStream implements Reset {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static final Semaphore flushDateSemaphore = new Semaphore(1);
    private static final Date currentDate = new Date(0);
    protected static byte[] date;
    private static String SERVER_ALIAS_NAME = "smart-http";

    static {
        String aliasServer = System.getProperty("smartHttp.server.alias");
        if (aliasServer != null) {
            SERVER_ALIAS_NAME = aliasServer + "smart-http";
        }
        flushDate();
    }

    protected final AbstractResponse response;
    protected final WriteBuffer writeBuffer;
    protected final HttpRequest request;
    protected boolean committed = false;
    /**
     * 当前流是否完结
     */
    protected boolean closed = false;
    protected boolean chunked = false;

    public AbstractOutputStream(HttpRequest request, AbstractResponse response, WriteBuffer writeBuffer) {
        this.response = response;
        this.request = request;
        this.writeBuffer = writeBuffer;
    }

    protected static void flushDate() {
        if ((System.currentTimeMillis() - currentDate.getTime() > 990) && flushDateSemaphore.tryAcquire()) {
            try {
                currentDate.setTime(System.currentTimeMillis());
                AbstractOutputStream.date = ("\r\n" + HeaderNameEnum.DATE.getName() + ":" + sdf.format(currentDate) + "\r\n"
                        + HeaderNameEnum.SERVER.getName() + ":" + SERVER_ALIAS_NAME + "\r\n\r\n").getBytes();
            } finally {
                flushDateSemaphore.release();
            }
        }
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
        if (HttpMethodEnum.HEAD.getMethod() == request.getMethod()) {
            throw new UnsupportedOperationException(request.getMethod() + " can not write http body");
        }
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
        if (HttpMethodEnum.HEAD.getMethod().equals(request.getMethod())) {
            throw new UnsupportedOperationException(request.getMethod() + " can not write http body");
        }
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

    protected final void writeHeader() throws IOException {
        if (response.getHeaders() != null) {
            for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
                HeaderValue headerValue = entry.getValue();
                while (headerValue != null) {
                    writeBuffer.write(getHeaderNameBytes(entry.getKey()));
                    writeBuffer.write(getBytes(headerValue.getValue()));
                    headerValue = headerValue.getNextValue();
                }
            }
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

    public final void reset() {
        committed = closed = chunked = false;
    }

    public final boolean isClosed() {
        return closed;
    }
}
