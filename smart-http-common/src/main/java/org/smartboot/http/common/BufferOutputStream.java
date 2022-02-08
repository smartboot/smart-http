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
import java.util.concurrent.Semaphore;

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
//    protected WriteCache writeCache;
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
        writeHeader();
        if (chunked) {
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
        writeHeader();
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
        writeHeader();
        writeBuffer.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            throw new IOException("outputStream has already closed");
        }
        writeHeader();

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


    protected abstract void writeHeader() throws IOException;

    protected abstract void check();

    protected static class WriteCache {
        private final byte[] cacheData;
        private final Semaphore semaphore = new Semaphore(1);
        private long cacheTime;


        public WriteCache(long cacheTime, byte[] data) {
            this.cacheTime = cacheTime;
            this.cacheData = data;
        }

        public long getCacheTime() {
            return cacheTime;
        }

        public void setCacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }

        public byte[] getCacheData() {
            return cacheData;
        }

    }
}
