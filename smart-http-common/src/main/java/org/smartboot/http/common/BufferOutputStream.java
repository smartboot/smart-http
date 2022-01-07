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
import org.smartboot.http.common.utils.GzipUtils;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.AioSession;
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
    protected final AioSession session;
    protected final WriteBuffer writeBuffer;
    protected boolean committed = false;
    protected boolean chunked = false;
    protected boolean gzip = false;
    protected WriteCache writeCache;
    /**
     * 当前流是否完结
     */
    private boolean closed = false;

    public BufferOutputStream(AioSession session) {
        this.session = session;
        this.writeBuffer = session.writeBuffer();
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
        check();
        writeHead();
        if (writeCache != null) {
            System.arraycopy(b, off, writeCache.cacheData, writeCache.bodyPosition, len);
            writeCache.bodyPosition += len;
            if (writeCache.bodyPosition == writeCache.cacheData.length) {
                writeBuffer.write(writeCache.cacheData, 0, writeCache.bodyPosition);
                writeCache = null;
            }
        } else if (chunked) {
            if (gzip) {
                b = GzipUtils.compress(b, off, len);
                off = 0;
                len = b.length;
            }
            byte[] start = getBytes(Integer.toHexString(len) + "\r\n");
            writeBuffer.write(start);
            writeBuffer.write(b, off, len);
            writeBuffer.write(Constant.CRLF_BYTES);
        } else {
            writeBuffer.write(b, off, len);
        }
    }

    /**
     * 直接输出，不执行编码
     *
     * @param b
     * @param off
     * @param len
     */
    public final void directWrite(byte[] b, int off, int len) throws IOException {
        writeBuffer.write(b, off, len);
    }

    public final void write(ByteBuffer buffer) throws IOException {
        write(VirtualBuffer.wrap(buffer));
    }

    public final void write(VirtualBuffer virtualBuffer) throws IOException {
        check();
        if (writeCache != null) {
            flush();
        } else {
            writeHead();
        }
        if (chunked) {
            byte[] start = getBytes(Integer.toHexString(virtualBuffer.buffer().remaining()) + "\r\n");
            writeBuffer.write(start);
            writeBuffer.write(virtualBuffer);
            writeBuffer.write(Constant.CRLF_BYTES);
        } else {
            writeBuffer.write(virtualBuffer);
        }
    }

    @Override
    public final void flush() throws IOException {
        writeHead();
        if (writeCache != null) {
            writeBuffer.write(writeCache.cacheData, 0, writeCache.bodyPosition);
            writeCache = null;
        }
        writeBuffer.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            throw new IOException("outputStream has already closed");
        }
        writeHead();
        if (writeCache != null) {
            writeBuffer.write(writeCache.cacheData, 0, writeCache.bodyPosition);
            writeCache = null;
        }

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
                extBytes = getBytes(name + ":");
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
        committed = closed = chunked = gzip = false;
    }


    protected abstract void writeHead() throws IOException;

    protected abstract void check();

    protected static class WriteCache {
        private final String contentType;
        private final int contentLength;
        /**
         * body的起始位置
         */
        private final int bodyInitPosition;
        private final byte[] cacheData;
        /**
         * body的当前输出位置
         */
        private int bodyPosition;
        private long cacheTime;


        public WriteCache(String contentType, long cacheTime, byte[] data, int contentLength) {
            this.contentType = contentType;
            this.cacheTime = cacheTime;
            this.contentLength = contentLength;
            this.cacheData = new byte[data.length + 2 + contentLength];
            this.bodyInitPosition = data.length + 2;
            System.arraycopy(data, 0, cacheData, 0, data.length);
            this.cacheData[data.length] = Constant.CR;
            this.cacheData[data.length + 1] = Constant.LF;
        }

        public String getContentType() {
            return contentType;
        }

        public int getContentLength() {
            return contentLength;
        }

        public int getBodyInitPosition() {
            return bodyInitPosition;
        }

        public void setBodyPosition(int bodyPosition) {
            this.bodyPosition = bodyPosition;
        }

        public long getCacheTime() {
            return cacheTime;
        }

        public void setCacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
        }

        public byte[] getCacheData() {
            return cacheData;
        }

    }
}
