/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.server.BaseHttpRequest;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public class HttpMethodDecoder implements Decoder {

    private final HttpUriDecoder decoder = new HttpUriDecoder();

    @Override
    public Decoder deocde(ByteBuffer byteBuffer, char[] cacheChars, AioSession<BaseHttpRequest> aioSession, BaseHttpRequest request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.SP, cacheChars, true);
        if (length > 0) {
            String method = StringUtils.convertToString(cacheChars, length, StringUtils.String_CACHE_URL);
            request.setMethod(method);
            return decoder.deocde(byteBuffer, cacheChars, aioSession, request);
        } else {
            return this;
        }
    }
}
