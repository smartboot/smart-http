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
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.socket.transport.WriteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends AbstractServerOutputStream {
    /**
     * key:status+contentType
     */
    private static final Map<String, byte[]>[] CACHE_CONTENT_TYPE_AND_LENGTH = new Map[512];
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

    @Override
    protected final byte[] getHeadPart() {
        HttpStatus httpStatus = response.getHttpStatus();
        int contentLength = response.getContentLength();
        String contentType = response.getContentType();
        chunked = contentLength < 0;
        byte[] data = null;
        //成功消息优先从缓存中加载
        boolean cache = httpStatus == HttpStatus.OK;
        //此处用 == 性能更高
        boolean http10 = HttpProtocolEnum.HTTP_10.getProtocol() == request.getProtocol();
        if (cache && !http10) {
            if (chunked) {
                data = CACHE_CHUNKED_AND_LENGTH.get(contentType);
            } else if (contentLength < CACHE_CONTENT_TYPE_AND_LENGTH.length) {
                data = CACHE_CONTENT_TYPE_AND_LENGTH[contentLength].get(contentType);
            }
            if (data != null) {
                return data;
            }
        }

        String str = request.getProtocol() + httpStatus.getHttpStatusLine() + "\r\n"
                + HeaderNameEnum.CONTENT_TYPE.getName() + ":" + contentType;
        if (contentLength >= 0) {
            str += "\r\n" + HeaderNameEnum.CONTENT_LENGTH.getName() + ":" + contentLength;
        } else if (chunked) {
            str += "\r\n" + HeaderNameEnum.TRANSFER_ENCODING.getName() + ":" + HeaderValueEnum.CHUNKED.getName();
        }
        data = str.getBytes();
        //缓存响应头
        if (cache && !http10) {
            if (chunked) {
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

}
