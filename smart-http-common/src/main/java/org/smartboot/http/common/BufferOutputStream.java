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
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/7
 */
public abstract class BufferOutputStream extends OutputStream implements Reset {
    private static final Map<String, byte[]> HEADER_NAME_EXT_MAP = new ConcurrentHashMap<>();
    protected final AioSession session;
    protected final WriteBuffer writeBuffer;
    protected boolean committed = false;
    protected boolean chunkedSupport = true;
    /**
     * 当前流是否完结
     */
    protected boolean closed = false;

    private Supplier<Map<String, String>> trailerSupplier;

    public BufferOutputStream(AioSession session) {
        this.session = session;
        this.writeBuffer = session.writeBuffer();
    }

    /**
     * 不推荐使用该方法，此方法性能不佳
     * @param b   the <code>byte</code>.
     * @throws IOException
     */
    @Override
    public final void write(int b) throws IOException {
        writeHeader(HeaderWriteSource.WRITE);
        if (chunkedSupport) {
            writeBuffer.write((Integer.toHexString(1) + "\r\n").getBytes());
            writeBuffer.write(b);
            writeBuffer.write(Constant.CRLF_BYTES);
        } else {
            writeBuffer.write(b);
        }
    }

    /**
     * 输出Http响应
     *
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    public void write(byte[] b, int off, int len) throws IOException {
        writeHeader(HeaderWriteSource.WRITE);

        if (len == 0) {
            return;
        }

        if (chunkedSupport) {
            byte[] start = (Integer.toHexString(len) + "\r\n").getBytes();
            writeBuffer.write(start);
            writeBuffer.write(b, off, len);
            writeBuffer.write(Constant.CRLF_BYTES);
        } else {
            writeBuffer.write(b, off, len);
        }
    }

    public final void write(byte[] b, int off, int len, Consumer<BufferOutputStream> consumer) throws IOException {
        writeHeader(HeaderWriteSource.WRITE);
        if (chunkedSupport) {
            byte[] start = (Integer.toHexString(len) + "\r\n").getBytes();
            writeBuffer.write(start);
            writeBuffer.write(b, off, len);
            writeBuffer.write(Constant.CRLF_BYTES, 0, 2, writeBuffer -> consumer.accept(BufferOutputStream.this));
        } else {
            writeBuffer.write(b, off, len, writeBuffer -> consumer.accept(BufferOutputStream.this));
        }
    }

    public final void transferFrom(ByteBuffer buffer, Consumer<BufferOutputStream> consumer) throws IOException {
        writeHeader(HeaderWriteSource.WRITE);
        if (!chunkedSupport) {
            writeBuffer.transferFrom(buffer, writeBuffer -> consumer.accept(BufferOutputStream.this));
            return;
        }
        byte[] start = (Integer.toHexString(buffer.remaining()) + "\r\n").getBytes();
        if (buffer.position() >= start.length) {
            buffer.put(start, buffer.position() - start.length, start.length);
            buffer.position(buffer.position() - start.length);
        } else {
            writeBuffer.write(start);
        }
        if (buffer.capacity() - buffer.limit() >= Constant.CRLF_BYTES.length) {
            buffer.put(Constant.CRLF_BYTES, buffer.limit(), Constant.CRLF_BYTES.length);
            buffer.limit(buffer.limit() + Constant.CRLF_BYTES.length);
            writeBuffer.transferFrom(buffer, writeBuffer -> consumer.accept(BufferOutputStream.this));
        } else {
            writeBuffer.transferFrom(buffer, writeBuffer -> {
                try {
                    writeBuffer.write(Constant.CRLF_BYTES, 0, 2, buffer1 -> consumer.accept(BufferOutputStream.this));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    @Override
    public final void flush() throws IOException {
        writeHeader(HeaderWriteSource.FLUSH);
        writeBuffer.flush();
    }

    @Override
    public final void close() throws IOException {
        if (closed) {
            throw new IOException("outputStream has already closed");
        }
        writeHeader(HeaderWriteSource.CLOSE);

        if (chunkedSupport) {
            if (trailerSupplier != null) {
                writeBuffer.write("0\r\n".getBytes());
                Map<String, String> map = trailerSupplier.get();
                for (String key : map.keySet()) {
                    writeBuffer.write((key + ":" + map.get(key) + "\r\n").getBytes());
                }
                writeBuffer.write(Constant.CRLF_BYTES);
            } else {
                writeBuffer.write(Constant.CHUNKED_END_BYTES);
            }
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
        committed = closed = false;
        chunkedSupport = true;
    }


    protected abstract void writeHeader(HeaderWriteSource source) throws IOException;

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

    public void disableChunked() {
        this.chunkedSupport = false;
    }

    public boolean isCommitted() {
        return committed;
    }

    public boolean isChunkedSupport() {
        return chunkedSupport;
    }

    protected enum HeaderWriteSource {
        WRITE, FLUSH, CLOSE
    }

    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        this.trailerSupplier = supplier;
    }

    public Supplier<Map<String, String>> getTrailerFields() {
        return trailerSupplier;
    }
}
