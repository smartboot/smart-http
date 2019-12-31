/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.utils.CharsetUtil;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.HeaderNameEnum;
import org.smartboot.http.utils.HttpHeaderConstant;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "flushDate");
            thread.setDaemon(true);
            return thread;
        }
    });
    private static String SERVER_ALIAS_NAME = "smart-http";
    private static SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static Date currentDate = new Date();
    private static byte[] date;

    static {
        for (int i = 0; i < CACHE_CONTENT_TYPE_AND_LENGTH.length; i++) {
            CACHE_CONTENT_TYPE_AND_LENGTH[i] = new HashMap<>();
        }
        String aliasServer = System.getProperty("smartHttp.server.alias");
        if (aliasServer != null) {
            SERVER_ALIAS_NAME = aliasServer + "smart-http";
        }
        flushDate();
        SCHEDULED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                HttpOutputStream.flushDate();
            }
        }, 900, 900, TimeUnit.MILLISECONDS);
    }

    private final Http11Response response;
    private final Http11Request request;
    private final OutputStream outputStream;
    private boolean committed = false, closed = false;
    private boolean chunked = false;

    public HttpOutputStream(Http11Request request, Http11Response response, OutputStream outputStream) {
        this.response = response;
        this.request = request;
        this.outputStream = outputStream;
    }

    private static void flushDate() {
        currentDate.setTime(System.currentTimeMillis());
        HttpOutputStream.date = ("\r\n" + HttpHeaderConstant.Names.DATE + ":" + sdf.format(currentDate) + "\r\n"
                + HttpHeaderConstant.Names.SERVER + ":" + SERVER_ALIAS_NAME + "\r\n\r\n").getBytes();
    }

    void reset() {
        committed = closed = chunked = false;
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
        if (HttpMethodEnum.HEAD == request.getMethodEnum()) {
            throw new UnsupportedOperationException(request.getMethodEnum() + " can not write http body");
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
        if (committed) {
            return;
        }
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
        if (response.getHeaders() != null) {
            for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
                HeaderValue headerValue = entry.getValue();
                while (headerValue != null) {
                    outputStream.write(getHeaderNameBytes(entry.getKey()));
                    outputStream.write(getBytes(headerValue.getValue()));
                    headerValue = headerValue.getNextValue();
                }
            }
        }

        /**
         * RFC2616 3.3.1
         * 只能用 RFC 1123 里定义的日期格式来填充头域 (header field)的值里用到 HTTP-date 的地方
         */
        outputStream.write(date);
        committed = true;
    }

    private byte[] getHeadPart(HttpStatus httpStatus, String contentType, int contentLength) {
        chunked = contentLength < 0;
        byte[] data = null;
        //成功消息优先从缓存中加载
        if (httpStatus == HttpStatus.OK) {
            if (chunked) {
                data = CACHE_CHUNKED_AND_LENGTH.get(contentType);
            } else if (contentLength >= 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                data = CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].get(contentType);
            }
            if (data != null) {
                return data;
            }
        }

        String str = httpStatus.getHttpStatusLine() + "\r\n"
                + HttpHeaderConstant.Names.CONTENT_TYPE + ":" + contentType;
        if (contentLength >= 0) {
            str += "\r\n" + HttpHeaderConstant.Names.CONTENT_LENGTH + ":" + contentLength;
        } else if (chunked) {
            str += "\r\n" + HttpHeaderConstant.Names.TRANSFER_ENCODING + ":" + HttpHeaderConstant.Values.CHUNKED;
        }
        data = str.getBytes();
        //缓存响应头
        if (httpStatus == HttpStatus.OK) {
            if (chunked) {
                CACHE_CHUNKED_AND_LENGTH.put(contentType, data);
            } else if (contentLength >= 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].put(contentType, data);
            }
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
        writeHead();
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            throw new IOException("outputStream has already closed");
        }
        writeHead();

        if (chunked) {
            outputStream.write(CHUNKED_END_BYTES);
        }
        closed = true;
    }


    private byte[] getBytes(String str) {
        return str.getBytes(CharsetUtil.US_ASCII);
    }

    public boolean isClosed() {
        return closed;
    }
}
