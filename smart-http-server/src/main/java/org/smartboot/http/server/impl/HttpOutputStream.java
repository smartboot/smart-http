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
import org.smartboot.http.common.utils.TimerUtils;

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
    private static final int CACHE_LIMIT = 512;
    /**
     * key:status+contentType
     */
    private static final ThreadLocal<Map<String, WriteCache>[]> CACHE_CONTENT_TYPE_AND_LENGTH = new ThreadLocal<Map<String, WriteCache>[]>() {
        @Override
        protected Map<String, WriteCache>[] initialValue() {
            Map<String, WriteCache>[] mapArray = new Map[CACHE_LIMIT];
            for (int i = 0; i < CACHE_LIMIT; i++) {
                mapArray[i] = new HashMap<>();
            }
            return mapArray;
        }
    };
    private static final Date currentDate = new Date(0);
    private static final Semaphore flushDateSemaphore = new Semaphore(1);
    private static byte[] dateBytes;
    private static String date;

    static {
        flushDate();
    }

    public HttpOutputStream(HttpRequestImpl httpRequest, HttpResponseImpl response, Request request) {
        super(httpRequest, response, request);
    }

    private static long flushDate() {
        long currentTime = TimerUtils.currentTimeMillis();
        if ((currentTime - currentDate.getTime() > 1000) && flushDateSemaphore.tryAcquire()) {
            try {
                currentDate.setTime(currentTime);
                date = sdf.format(currentDate);
                dateBytes = date.getBytes();
            } finally {
                flushDateSemaphore.release();
            }
        }
        return currentTime;
    }

    @Override
    protected byte[] getHeadPart() {
        long currentTime = flushDate();
        int httpStatus = response.getHttpStatus();
        String reasonPhrase = response.getReasonPhrase();
        int contentLength = response.getContentLength();
        String contentType = response.getContentType();
        WriteCache data;
        writeCache = null;
        //成功消息优先从缓存中加载。启用缓存的条件：Http_200, contentLength<512,未设置过Header,Http/1.1
        boolean cache = httpStatus == HttpStatus.OK.value()
                && HttpStatus.OK.getReasonPhrase().equals(reasonPhrase)
                && contentLength > 0
                && contentLength < CACHE_LIMIT
                && !hasHeader();

        if (cache) {
            data = CACHE_CONTENT_TYPE_AND_LENGTH.get()[contentLength].get(contentType);
            if (data != null) {
                writeCache = data;
                writeCache.setBodyPosition(writeCache.getBodyInitPosition());
                if (currentTime - data.getCacheTime() > 1000) {
                    data.setCacheTime(currentTime);
                    System.arraycopy(dateBytes, 0, data.getCacheData(), data.getCacheData().length - contentLength - 4 - dateBytes.length, dateBytes.length);
                }
                //命中缓存，无需响应
                return null;
            }
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append(request.getProtocol()).append(Constant.SP_CHAR).append(httpStatus).append(Constant.SP_CHAR).append(reasonPhrase).append(Constant.CRLF);
        if (contentType != null) {
            sb.append(HeaderNameEnum.CONTENT_TYPE.getName()).append(Constant.COLON_CHAR).append(contentType).append(Constant.CRLF);
        }
        if (contentLength >= 0) {
            sb.append(HeaderNameEnum.CONTENT_LENGTH.getName()).append(Constant.COLON_CHAR).append(contentLength).append(Constant.CRLF);
        } else if (chunked) {
            sb.append(HeaderNameEnum.TRANSFER_ENCODING.getName()).append(Constant.COLON_CHAR).append(HeaderValueEnum.CHUNKED.getName()).append(Constant.CRLF);
        }

        if (configuration.serverName() != null && response.getHeader(HeaderNameEnum.SERVER.getName()) == null) {
            sb.append(SERVER_LINE);
        }
        sb.append(HeaderNameEnum.DATE.getName()).append(Constant.COLON_CHAR).append(date).append(Constant.CRLF);

        //缓存响应头
        if (cache) {
            CACHE_CONTENT_TYPE_AND_LENGTH.get()[contentLength].put(contentType, new WriteCache(contentType, currentTime, sb.toString().getBytes(), contentLength));
        }
        return hasHeader() ? sb.toString().getBytes() : sb.append(Constant.CRLF).toString().getBytes();
    }
}
