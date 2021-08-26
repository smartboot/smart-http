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
import org.smartboot.http.common.utils.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends AbstractOutputStream {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    /**
     * key:status+contentType
     */
    private static final Map<String, CacheHeader>[] CACHE_CONTENT_TYPE_AND_LENGTH = new Map[512];
    /**
     * Key：status+contentType
     */
    private static final Map<String, CacheHeader> CACHE_CHUNKED_AND_LENGTH = new HashMap<>();
    private static final Date currentDate = new Date(0);
    private static final Semaphore flushDateSemaphore = new Semaphore(1);
    private static byte[] dateBytes;
    private static String date;


    static {
        flushDate();
    }

    static {
        for (int i = 0; i < CACHE_CONTENT_TYPE_AND_LENGTH.length; i++) {
            CACHE_CONTENT_TYPE_AND_LENGTH[i] = new HashMap<>();
        }
    }

    public HttpOutputStream(HttpRequestImpl httpRequest, HttpResponseImpl response, Request request) {
        super(httpRequest, response, request);
    }

    private static void flushDate() {
        if ((System.currentTimeMillis() - currentDate.getTime() > 990) && flushDateSemaphore.tryAcquire()) {
            try {
                currentDate.setTime(System.currentTimeMillis());
                date = sdf.format(currentDate);
                dateBytes = date.getBytes();
            } finally {
                flushDateSemaphore.release();
            }
        }
    }

    @Override
    protected byte[] getHeadPart() {
        flushDate();
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
                    if (currentDate.getTime() - data.cacheTime > 1000) {
                        data.cacheTime = currentDate.getTime();
                        System.arraycopy(dateBytes, 0, data.partialHeaderData, data.partialHeaderData.length - 2 - dateBytes.length, dateBytes.length);
                    }
                    return data.partialHeaderData;
                } else {
                    if (currentDate.getTime() - data.cacheTime > 1000) {
                        data.cacheTime = currentDate.getTime();
                        System.arraycopy(dateBytes, 0, data.fullHeaderData, data.fullHeaderData.length - 4 - dateBytes.length, dateBytes.length);
                    }
                    return data.fullHeaderData;
                }
            }
        }

        StringBuilder sb = new StringBuilder(request.getProtocol());
        sb.append(Constant.SP_CHAR).append(httpStatus).append(Constant.SP_CHAR).append(reasonPhrase).append(Constant.CRLF);
        if (contentType != null) {
            sb.append(HeaderNameEnum.CONTENT_TYPE.getName()).append(Constant.COLON_CHAR).append(contentType).append(Constant.CRLF);
        }
        if (contentLength >= 0) {
            sb.append(HeaderNameEnum.CONTENT_LENGTH.getName()).append(Constant.COLON_CHAR).append(contentLength).append(Constant.CRLF);
        } else if (chunked) {
            sb.append(HeaderNameEnum.TRANSFER_ENCODING.getName()).append(Constant.COLON_CHAR).append(HeaderValueEnum.CHUNKED.getName()).append(Constant.CRLF);
        }

        if (configuration.serverName() != null && response.getHeader(HeaderNameEnum.SERVER.getName()) == null) {
            sb.append(HeaderNameEnum.SERVER.getName()).append(Constant.COLON_CHAR).append(configuration.serverName()).append(Constant.CRLF);
        }
        sb.append(HeaderNameEnum.DATE.getName()).append(Constant.COLON_CHAR).append(date).append(Constant.CRLF);

        //缓存响应头
        if (cache) {
            if (chunked) {
                CACHE_CHUNKED_AND_LENGTH.put(contentType, new CacheHeader(currentDate.getTime(), sb.toString().getBytes()));
            } else if (contentLength >= 0 && contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].put(contentType, new CacheHeader(currentDate.getTime(), sb.toString().getBytes()));
            }
        }
        return hasHeader() ? sb.toString().getBytes() : sb.append(Constant.CRLF).toString().getBytes();
    }

    private static class CacheHeader {
        long cacheTime;
        byte[] partialHeaderData;
        byte[] fullHeaderData;

        public CacheHeader(long cacheTime, byte[] data) {
            this.cacheTime = cacheTime;
            this.partialHeaderData = data;
            this.fullHeaderData = new byte[data.length + 2];
            System.arraycopy(data, 0, fullHeaderData, 0, data.length);
            this.fullHeaderData[fullHeaderData.length - 2] = Constant.CR;
            this.fullHeaderData[fullHeaderData.length - 1] = Constant.LF;
        }
    }
}
