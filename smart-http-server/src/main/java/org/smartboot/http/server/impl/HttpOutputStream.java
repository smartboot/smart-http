/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends AbstractOutputStream {
    private static final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH));
    /**
     * key:status+contentType
     */
    private static final Map<String, CacheHeader>[] CACHE_CONTENT_TYPE_AND_LENGTH = new Map[512];
    /**
     * Key：status+contentType
     */
    private static final Map<String, CacheHeader> CACHE_CHUNKED_AND_LENGTH = new HashMap<>();
    private static final Date currentDate = new Date(0);

    static {
        for (int i = 0; i < CACHE_CONTENT_TYPE_AND_LENGTH.length; i++) {
            CACHE_CONTENT_TYPE_AND_LENGTH[i] = new HashMap<>();
        }
    }

    public HttpOutputStream(HttpRequestImpl httpRequest, HttpResponseImpl response, Request request) {
        super(httpRequest, response, request);
    }

    @Override
    protected byte[] getHeadPart() {
        int httpStatus = response.getHttpStatus();
        String reasonPhrase = response.getReasonPhrase();
        int contentLength = response.getContentLength();
        String contentType = response.getContentType();
        CacheHeader data = null;
        //成功消息优先从缓存中加载
        boolean cache = httpStatus == HttpStatus.OK.value() && HttpStatus.OK.getReasonPhrase().equals(reasonPhrase);

        if (cache) {
            if (chunked) {
                data = CACHE_CHUNKED_AND_LENGTH.get(contentType);
            } else if (contentLength > 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                data = CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].get(contentType);
            }

            if (data != null) {
                if (hasHeader()) {
                    System.arraycopy(AbstractOutputStream.date, 0, data.data, data.data.length - 2 - date.length, date.length);
                    return data.data;
                } else {
                    System.arraycopy(AbstractOutputStream.date, 0, data.data2, data.data2.length - 4 - date.length, date.length);
                    return data.data2;
                }
            }
        }

        StringBuilder sb = new StringBuilder(request.getProtocol());
        sb.append(' ').append(httpStatus).append(' ').append(reasonPhrase).append("\r\n");
        if (contentType != null) {
            sb.append(HeaderNameEnum.CONTENT_TYPE.getName()).append(':').append(contentType).append("\r\n");
        }
        if (contentLength >= 0) {
            sb.append(HeaderNameEnum.CONTENT_LENGTH.getName()).append(':').append(contentLength).append("\r\n");
        } else if (chunked) {
            sb.append(HeaderNameEnum.TRANSFER_ENCODING.getName()).append(':').append(HeaderValueEnum.CHUNKED.getName()).append("\r\n");
        }

        if (configuration.serverName() != null && response.getHeader(HeaderNameEnum.SERVER.getName()) == null) {
            sb.append(HeaderNameEnum.SERVER.getName()).append(':').append(configuration.serverName()).append("\r\n");
        }
        currentDate.setTime(System.currentTimeMillis());
        sb.append(HeaderNameEnum.DATE.getName()).append(':').append(sdf.get().format(currentDate)).append("\r\n");

        //缓存响应头
        if (cache) {
            if (chunked) {
                CACHE_CHUNKED_AND_LENGTH.put(contentType, new CacheHeader(sb.toString().getBytes()));
            } else if (contentLength >= 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].put(contentType, new CacheHeader(sb.toString().getBytes()));
            }
        }
        return hasHeader() ? sb.toString().getBytes() : sb.append("\r\n").toString().getBytes();
    }

    private static class CacheHeader {
        byte[] data;
        byte[] data2;

        public CacheHeader(byte[] data) {
            this.data = data;
            this.data2 = new byte[data.length + 2];
            System.arraycopy(data, 0, data2, 0, data.length);
            this.data2[data2.length - 2] = '\r';
            this.data2[data2.length - 1] = '\n';
        }
    }
}
