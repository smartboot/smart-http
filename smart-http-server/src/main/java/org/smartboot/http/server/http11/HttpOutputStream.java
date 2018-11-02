/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpOutputStream.java
 * Date: 2018-02-17
 * Author: sandao
 */

package org.smartboot.http.server.http11;

import org.apache.commons.lang.StringUtils;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.server.handle.http11.ResponseHandle;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.buffer.ByteBuf;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends OutputStream {
    public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";
    public static final int DEFAULT_CACHE_SIZE = 512;
    private static final byte[] endChunked = new byte[]{'0', Consts.CR, Consts.LF, Consts.CR, Consts.LF};
    boolean chunkedEnd = false;
    private NoneOutputHttpResponseWrap response = new NoneOutputHttpResponseWrap();
    private AioSession aioSession;
    private ByteBuf cacheBuffer;
    private boolean committed = false, closed = false;
    private boolean chunked = false;
    private HttpRequest request;
    private ResponseHandle responseHandle;

    public HttpOutputStream(ResponseHandle responseHandle) {
        this.responseHandle = responseHandle;
    }

    void init(AioSession aioSession, DefaultHttpResponse response, HttpRequest request) {
        this.aioSession = aioSession;
        this.request = request;
        this.response.setResponse(response);
        cacheBuffer = aioSession.allocateBuf(DEFAULT_CACHE_SIZE);
        chunkedEnd = committed = closed = chunked = false;
    }

    @Override
    public final void write(int b) throws IOException {
        if (!committed) {
            writeHead();
            committed = true;
        }
        if (!cacheBuffer.buffer().hasRemaining()) {
            flush();
        }
        cacheBuffer.buffer().put((byte) b);
        if (!cacheBuffer.buffer().hasRemaining()) {
            flush();
        }
    }

    public final void write(byte b[], int off, int len) throws IOException {
        if (!committed) {
            writeHead();
            committed = true;
        }
        if (!cacheBuffer.buffer().hasRemaining()) {
            flush();
        }
        do {
            int min = len < cacheBuffer.buffer().remaining() ? len : cacheBuffer.buffer().remaining();
            cacheBuffer.buffer().put(b, off, min);
            off += min;
            len -= min;
            if (!cacheBuffer.buffer().hasRemaining()) {
                flush();
            }
        } while (len > 0);
    }

    public final void write(ByteBuffer buffer) throws IOException {
        if (!committed) {
            writeHead();
            committed = true;
        }
        if (!cacheBuffer.buffer().hasRemaining()) {
            flush();
        }
        if (cacheBuffer.buffer().remaining() >= buffer.remaining()) {
            cacheBuffer.buffer().put(buffer);
        } else {
            while (cacheBuffer.buffer().hasRemaining()) {
                cacheBuffer.buffer().put(buffer.get());
            }
            write(buffer);
        }

        if (!cacheBuffer.buffer().hasRemaining()) {
            flush();
        }
    }

    private void writeHead() throws IOException {
        responseHandle.doHandle(request, response);//防止在handle中调用outputStream操作
        chunked = StringUtils.equals(HttpHeaderConstant.Values.CHUNKED, response.getHeader(HttpHeaderConstant.Names.TRANSFER_ENCODING));

        cacheBuffer.buffer().put(getBytes(request.getProtocol()))
                .put(Consts.SP)
                .put(response.getHttpStatus().getValueStringBytes())
                .put(Consts.SP)
                .put(response.getHttpStatus().getReasonPhraseBytes())
                .put(Consts.CRLF);

        if (response.getHeader(HttpHeaderConstant.Names.CONTENT_TYPE) == null) {
            response.setHeader(HttpHeaderConstant.Names.CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        }

        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            byte[] headKey = getBytes(entry.getKey());
            byte[] headVal = getBytes(entry.getValue());

            int needLength = headKey.length + headVal.length + 3;
            if (cacheBuffer.buffer().remaining() < needLength) {
                cacheBuffer.buffer().flip();
                aioSession.write(cacheBuffer);
                cacheBuffer = aioSession.allocateBuf(DEFAULT_CACHE_SIZE);
            }
            cacheBuffer.buffer().put(headKey)
                    .put(Consts.COLON)
                    .put(headVal)
                    .put(Consts.CRLF);
        }
        if (cacheBuffer.buffer().remaining() >= 2) {
            cacheBuffer.buffer().put(Consts.CRLF);
        } else {
            cacheBuffer.buffer().flip();
            aioSession.write(cacheBuffer);
            cacheBuffer = aioSession.allocateBuf(DEFAULT_CACHE_SIZE);
            cacheBuffer.buffer().put(Consts.CRLF);
//            aioSession.write(ByteBuffer.wrap(new byte[]{Consts.CR, Consts.LF}));
        }
        if (chunked) {
            cacheBuffer.buffer().flip();
            aioSession.write(cacheBuffer);
            cacheBuffer = aioSession.allocateBuf(DEFAULT_CACHE_SIZE);
        }
    }

    @Override
    public void flush() throws IOException {
        if (!committed) {
            writeHead();
            committed = true;
        }
        cacheBuffer.buffer().flip();
        if (cacheBuffer.buffer().hasRemaining()) {
            ByteBuf buffer = null;
            if (chunked) {
                byte[] start = getBytes(Integer.toHexString(cacheBuffer.buffer().remaining()) + "\r\n");
                buffer = aioSession.allocateBuf(start.length + cacheBuffer.buffer().remaining() + Consts.CRLF.length + (chunkedEnd ? endChunked.length : 0));
                buffer.buffer().put(start).put(cacheBuffer.buffer()).put(Consts.CRLF);
                if (chunkedEnd) {
                    buffer.buffer().put(endChunked);
                }
                buffer.buffer().flip();
            } else {
                buffer = cacheBuffer;
                cacheBuffer = aioSession.allocateBuf(cacheBuffer.buffer().capacity());
            }
            aioSession.write(buffer);
        } else if (chunked && chunkedEnd) {
            ByteBuf byteBuf=aioSession.allocateBuf(endChunked.length);
            byteBuf.buffer().put(endChunked).flip();
            aioSession.write(byteBuf);
        }
        cacheBuffer.buffer().clear();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        chunkedEnd = true;
        flush();
        closed = true;
    }

    private byte[] getBytes(String str) {
        return str.getBytes(Consts.DEFAULT_CHARSET);
//        return str.getBytes();
    }

}
