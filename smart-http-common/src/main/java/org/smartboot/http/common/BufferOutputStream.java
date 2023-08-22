/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BufferOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.io.ChunkedOutputStream;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPOutputStream;

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
    protected boolean body = false;
    protected boolean gzip = false;
    /**
     * 当前流是否完结
     */
    private boolean closed = false;

    private OutputStream chunkedOutputStream;

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
    public final void write(byte[] b, int off, int len) throws IOException {
        body = true;
        writeHeader();

        if (len == 0) {
            return;
        }

        if (chunked) {
            checkChunkedOutputStream();
            chunkedOutputStream.write(b, off, len);
        } else {
            writeBuffer.write(b, off, len);
        }
    }

    private void checkChunkedOutputStream() throws IOException {
        if (chunkedOutputStream == null) {
            chunkedOutputStream = new ChunkedOutputStream(writeBuffer);
            if (gzip) {
                chunkedOutputStream = new GZIPOutputStream(chunkedOutputStream);
            }
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
        if (len == 0) {
            return;
        }

        writeBuffer.write(b, off, len);
    }


    @Override
    public final void flush() throws IOException {
        writeHeader();
        writeBuffer.flush();
    }

    @Override
    public final void close() throws IOException {
        if (closed) {
            throw new IOException("outputStream has already closed");
        }
        writeHeader();

        if (chunked) {
            checkChunkedOutputStream();
            chunkedOutputStream.close();
            chunkedOutputStream = null;
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
        committed = closed = chunked = gzip = body = false;
    }


    protected abstract void writeHeader() throws IOException;

    protected static class WriteCache {
        private final byte[] cacheData;
        private final Semaphore semaphore = new Semaphore(1);
        private long expireTime;


        public WriteCache(long cacheTime, byte[] data) {
            this.expireTime = cacheTime;
            this.cacheData = data;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }

        public byte[] getCacheData() {
            return cacheData;
        }

    }
}
