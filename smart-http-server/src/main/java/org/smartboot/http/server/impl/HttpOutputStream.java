/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends AbstractOutputStream {
    /**
     * key:status+contentType
     */
    private static final Map<String, byte[]>[] CACHE_CONTENT_TYPE_AND_LENGTH = new Map[512];
    private static final byte[][] CACHE_TEXT_PLAIN_AND_LENGTH = new byte[512][];
    private static final byte[][] CACHE_JSON_AND_LENGTH = new byte[1024][];
    /**
     * Key：status+contentType
     */
    private static final Map<String, byte[]> CACHE_CHUNKED_AND_LENGTH = new HashMap<>();

    static {
        for (int i = 0; i < CACHE_CONTENT_TYPE_AND_LENGTH.length; i++) {
            CACHE_CONTENT_TYPE_AND_LENGTH[i] = new HashMap<>();
        }
    }

    public HttpOutputStream(HttpRequestImpl request, HttpResponseImpl response, WriteBuffer writeBuffer) {
        super(request, response, writeBuffer);
    }

    /**
     * 输出Http消息头
     *
     * @throws IOException
     */
    final protected void writeHead() throws IOException {
        if (committed) {
            return;
        }

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        writeBuffer.write(getHeadPart(response.getContentType()));

        //转换Cookie
        convertCookieToHeader(response);

        //输出Header部分
        writeHeader();

        /**
         * RFC2616 3.3.1
         * 只能用 RFC 1123 里定义的日期格式来填充头域 (header field)的值里用到 HTTP-date 的地方
         */
        flushDate();
        writeBuffer.write(date);
        committed = true;
    }

    private byte[] getHeadPart(String contentType) {
        HttpStatus httpStatus = response.getHttpStatus();
        int contentLength = response.getContentLength();
        chunked = contentLength < 0;
        byte[] data = null;
        //成功消息优先从缓存中加载
        boolean cache = httpStatus == HttpStatus.OK;
        boolean http10 = HttpProtocolEnum.HTTP_10.getProtocol().equals(request.getProtocol());
        if (cache && !http10) {
            if ("text/plain; charset=UTF-8" == contentType && contentLength < CACHE_TEXT_PLAIN_AND_LENGTH.length) {
                data = CACHE_TEXT_PLAIN_AND_LENGTH[contentLength];
            } else if ("application/json" == contentType && contentLength < CACHE_JSON_AND_LENGTH.length) {
                data = CACHE_JSON_AND_LENGTH[contentLength];
            } else if (chunked) {
                data = CACHE_CHUNKED_AND_LENGTH.get(contentType);
            } else if (contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                data = CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].get(contentType);
            }
            if (data != null) {
                return data;
            }
        }

        String str = request.getProtocol() + httpStatus.getHttpStatusLine() + "\r\n"
                + HttpHeaderConstant.Names.CONTENT_TYPE + ":" + contentType;
        if (contentLength >= 0) {
            str += "\r\n" + HttpHeaderConstant.Names.CONTENT_LENGTH + ":" + contentLength;
        } else if (chunked) {
            str += "\r\n" + HttpHeaderConstant.Names.TRANSFER_ENCODING + ":" + HttpHeaderConstant.Values.CHUNKED;
        }
        data = str.getBytes();
        //缓存响应头
        if (cache && !http10) {
            if ("text/plain; charset=UTF-8" == contentType && contentLength < CACHE_TEXT_PLAIN_AND_LENGTH.length) {
                CACHE_TEXT_PLAIN_AND_LENGTH[contentLength] = data;
            } else if ("application/json" == contentType && contentLength < CACHE_JSON_AND_LENGTH.length) {
                CACHE_JSON_AND_LENGTH[contentLength] = data;
            } else if (chunked) {
                CACHE_CHUNKED_AND_LENGTH.put(contentType, data);
            } else if (contentLength >= 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].put(contentType, data);
            }
        }
        // http 1.0 不支持 chunked
        if (chunked && http10) {
            chunked = false;
        }
        return data;
    }

    private void convertCookieToHeader(AbstractResponse response) {
        List<Cookie> cookies = response.getCookies();
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        cookies.forEach(cookie -> {
            response.addHeader(HttpHeaderConstant.Names.SET_COOKIE, cookie.toString());
        });

    }
}
