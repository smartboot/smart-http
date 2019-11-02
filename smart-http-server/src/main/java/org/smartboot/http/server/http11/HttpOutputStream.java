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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends OutputStream {
    /**
     * key:status+contentType
     */
    private static final Map<String, byte[]>[] CACHE_CONTENT_TYPE_AND_LENGTH = new Map[512];
    /**
     * Key：status+contentType
     */
    private static final Map<String, byte[]> CACHE_CHUNKED_AND_LENGTH = new HashMap<>();

    private static final byte[] CHUNKED_END_BYTES = "0\r\n\r\n".getBytes(CharsetUtil.US_ASCII);

    private static SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static Date currentDate = new Date();
    private static byte[] date;

    static {
        for (int i = 0; i < CACHE_CONTENT_TYPE_AND_LENGTH.length; i++) {
            CACHE_CONTENT_TYPE_AND_LENGTH[i] = new HashMap<>();
        }
        flushDate();
        new ResponseDateTimer();
    }

    private DefaultHttpResponse response;
    private OutputStream outputStream;
    private boolean committed = false, closed = false;
    private boolean chunked = false;

    private static void flushDate() {
        currentDate.setTime(System.currentTimeMillis());
        HttpOutputStream.date = ("\r\n" + HttpHeaderConstant.Names.DATE + ":" + sdf.format(currentDate) + "\r\n\r\n").getBytes();
    }


    void init(OutputStream outputStream, DefaultHttpResponse response) {
        this.outputStream = outputStream;
        this.response = response;
        committed = closed = chunked = false;
    }

    @Override
    public final void write(int b) throws IOException {
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
        if (!committed) {
            writeHead();
            committed = true;
        }
        if (chunked) {
            byte[] start = getBytes(Integer.toHexString(len) + "\r\n");
            outputStream.write(start);
            outputStream.write(b, off, len);
            outputStream.write(Consts.CRLF);
        } else {
            outputStream.write(b, off, len);
        }

    }

    /**
     * 输出Http消息头
     *
     * @throws IOException
     */
    private void writeHead() throws IOException {
        if (response.getHttpStatus() == null) {
            response.setHttpStatus(HttpStatus.OK);
        }
        String contentType = response.getContentType();
        if (contentType == null) {
            contentType = HttpHeaderConstant.Values.DEFAULT_CONTENT_TYPE;
        }
        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        outputStream.write(getHeadPart(response.getHttpStatus(), contentType, response.getContentLength()));

        //输出Header部分
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            outputStream.write(getHeaderNameBytes(entry.getKey()));
            outputStream.write(getBytes(entry.getValue()));
        }

        /**
         * RFC2616 3.3.1
         * 只能用 RFC 1123 里定义的日期格式来填充头域 (header field)的值里用到 HTTP-date 的地方
         */
        outputStream.write(date);
    }

    private byte[] getHeadPart(HttpStatus httpStatus, String contentType, int contentLength) {
        chunked = contentLength < 0;
        Map<String, byte[]> map = null;
        if (chunked) {
            map = CACHE_CHUNKED_AND_LENGTH;
        } else if (contentLength >= 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
            map = CACHE_CONTENT_TYPE_AND_LENGTH[contentLength];
        }

        String cacheKey = httpStatus.value() + contentType;
        byte[] data = null;
        if (map != null) {
            data = map.get(cacheKey);
            if (data != null) {
                return data;
            }
        }

        String str = httpStatus.getHttpStatusLine() + "\r\n"
                + HttpHeaderConstant.Names.CONTENT_TYPE + ":" + contentType + "\r\n"
                + HttpHeaderConstant.Names.SERVER + ":smart-http";
        if (contentLength >= 0) {
            str += "\r\n" + HttpHeaderConstant.Names.CONTENT_LENGTH + ":" + contentLength;
        } else if (chunked) {
            str += "\r\n" + HttpHeaderConstant.Names.TRANSFER_ENCODING + ":" + HttpHeaderConstant.Values.CHUNKED;
        }
        data = str.getBytes();
        //缓存响应头
        if (chunked) {
            CACHE_CHUNKED_AND_LENGTH.put(cacheKey, data);
        } else if (contentLength >= 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
            CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].put(cacheKey, data);
        }
        return data;

    }

    private byte[] getHeaderNameBytes(String name) {
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

    @Override
    public void flush() throws IOException {
        if (!committed) {
            writeHead();
            committed = true;
        }
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            throw new IOException("outputStream has already closed");
        }

        if (chunked) {
            outputStream.write(CHUNKED_END_BYTES);
        }
        flush();
        closed = true;
    }

    private byte[] getBytes(String str) {
        return str.getBytes(CharsetUtil.US_ASCII);
    }

    public boolean isClosed() {
        return closed;
    }

    public static class ResponseDateTimer extends QuickTimerTask {

        @Override
        protected long getPeriod() {
            return 900;
        }

        @Override
        public void run() {
            HttpOutputStream.flushDate();
        }
    }
}
