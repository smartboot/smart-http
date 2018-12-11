/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpOutputStream.java
 * Date: 2018-02-17
 * Author: sandao
 */

package org.smartboot.http.server.http11;

import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.utils.CharsetUtil;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.HeaderNameEnum;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.util.QuickTimerTask;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends OutputStream {

    public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";
    public static final byte[] DEFAULT_CONTENT_TYPE_BYTES = "text/html; charset=utf-8".getBytes(Consts.DEFAULT_CHARSET);
    public static final int DEFAULT_CACHE_SIZE = 512;
    private static final byte[] CHUNK_LINE = (HttpHeaderConstant.Names.TRANSFER_ENCODING + ":" + HttpHeaderConstant.Values.CHUNKED + "\r\n").getBytes();
    private static final String SERVER_LIN_STR = HttpHeaderConstant.Names.SERVER + ":smart-http\r\n\r\n";
    private static final byte[] SERVE_LINE = SERVER_LIN_STR.getBytes();
    private static final byte[] CONTENT_TYPE_LINE = ("\r\n" + HttpHeaderConstant.Names.CONTENT_TYPE + ":text/html; charset=utf-8").getBytes();
    private static final byte[] endChunked = new byte[]{'0', Consts.CR, Consts.LF, Consts.CR, Consts.LF};

    private static final byte[][] CONTENT_LENGTH_CACHE = new byte[100][];
    private static SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static Date currentDate = new Date();
    private static byte[] date;

    static {
        for (int i = 0; i < CONTENT_LENGTH_CACHE.length; i++) {
            CONTENT_LENGTH_CACHE[i] = ("\r\n" + HttpHeaderConstant.Names.CONTENT_LENGTH + ":" + i).getBytes();
        }
        flushDate();
        new ResponseDateTimer();
    }

    boolean chunkedEnd = false;
    private DefaultHttpResponse response;
    private OutputStream outputStream;
    private boolean committed = false, closed = false;
    private boolean chunked = false;

    private static void flushDate() {
        currentDate.setTime(System.currentTimeMillis());
        HttpOutputStream.date = ("\r\n" + HttpHeaderConstant.Names.DATE + ":" + sdf.format(currentDate) + "\r\n" + SERVER_LIN_STR).getBytes();
    }

    void init(OutputStream outputStream, DefaultHttpResponse response) {
        this.outputStream = outputStream;
        this.response = response;
        chunkedEnd = committed = closed = chunked = false;
    }

    @Override
    public final void write(int b) throws IOException {
//        if (!committed) {
//            writeHead();
//            committed = true;
//        }
//        if (!cacheBuffer.buffer().hasRemaining()) {
//            flush();
//        }
//        cacheBuffer.buffer().put((byte) b);
//        if (!cacheBuffer.buffer().hasRemaining()) {
//            flush();
//        }
    }

    public final void write(byte b[], int off, int len) throws IOException {
        if (!committed) {
            writeHead();
            committed = true;
        }
        if (chunked) {

        } else {
            outputStream.write(b, off, len);
        }

    }

    private void writeHead() throws IOException {
        if (response.getHttpStatus() == null) {
            response.setHttpStatus(HttpStatus.OK);
        }
//        outputStream.reset();
        outputStream.write(response.getHttpStatus().getLineBytes());
        if (!response.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
                outputStream.write(getHeaderNameBytes(entry.getKey()));
                outputStream.write(getBytes(entry.getValue()));
//                outputStream.write(Consts.CRLF);
            }
        }
        if (response.getContentType() == null) {
            outputStream.write(CONTENT_TYPE_LINE);
        } else {
            outputStream.write(HeaderNameEnum.CONTENT_TYPE.getBytesWithColon());
            outputStream.write(getBytes(response.getContentType()));
//            outputStream.write(Consts.CRLF);
//            outputStream.write((HttpHeaderConstant.Names.CONTENT_TYPE + ":" + response.getContentType() + "\r\n").getBytes());
        }
        if (response.getHttpStatus() == HttpStatus.OK) {
            if (response.getContentLength() >= 0 && response.getContentLength() < CONTENT_LENGTH_CACHE.length) {
                outputStream.write(CONTENT_LENGTH_CACHE[response.getContentLength()]);
            } else if (response.getContentLength() >= CONTENT_LENGTH_CACHE.length) {
                outputStream.write(HeaderNameEnum.CONTENT_LENGTH.getBytesWithColon());
                outputStream.write((response.getContentLength() + "").getBytes());
//                outputStream.write(Consts.CRLF);
            } else if (response.getTransferEncoding() == null) {
                chunked = true;
                outputStream.write(CHUNK_LINE);
            } else {
                throw new RuntimeException();
            }
        }


        /**
         * RFC2616 3.3.1
         * 只能用 RFC 1123 里定义的日期格式来填充头域 (header field)的值里用到 HTTP-date 的地方
         */
//        if (date != null) {
        outputStream.write(date);
//        } else {
//            outputStream.write(SERVE_LINE);
//        }
    }

    private byte[] getHeaderNameBytes(String name) {
        for (HeaderNameEnum headerNameEnum : HttpHeaderConstant.HEADER_NAME_ENUM_LIST) {
            if (headerNameEnum.getName().equals(name)) {
                return headerNameEnum.getBytesWithColon();
            }
        }
        return getBytes("\r\n" + name + ":");
    }

    @Override
    public void flush() throws IOException {
        if (!committed) {
            writeHead();
            committed = true;
        }
//        System.out.println("flash");
        outputStream.flush();
//        cacheBuffer.buffer().flip();
//        if (cacheBuffer.buffer().hasRemaining()) {
//            ByteBuf buffer = null;
//            if (chunked) {
//                byte[] start = getBytes(Integer.toHexString(cacheBuffer.buffer().remaining()) + "\r\n");
//                buffer = aioSession.allocateBuf(start.length + cacheBuffer.buffer().remaining() + Consts.CRLF.length + (chunkedEnd ? endChunked.length : 0));
//                buffer.buffer().put(start).put(cacheBuffer.buffer()).put(Consts.CRLF);
//                if (chunkedEnd) {
//                    buffer.buffer().put(endChunked);
//                }
//                buffer.buffer().flip();
//            } else {
//                buffer = cacheBuffer;
//                cacheBuffer = aioSession.allocateBuf(cacheBuffer.buffer().capacity());
//            }
//            aioSession.write(buffer);
//        } else if (chunked && chunkedEnd) {
//            ByteBuf byteBuf = aioSession.allocateBuf(endChunked.length);
//            byteBuf.buffer().put(endChunked).flip();
//            aioSession.write(byteBuf);
//        }
//        cacheBuffer.buffer().clear();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        chunkedEnd = true;
//        flush();
        closed = true;
    }

    private byte[] getBytes(String str) {
        return str.getBytes(CharsetUtil.US_ASCII);
    }

    public static class ResponseDateTimer extends QuickTimerTask {

        @Override
        protected long getPeriod() {
            return 5000;
        }

        @Override
        public void run() {
            HttpOutputStream.flushDate();
        }
    }
}
