/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.server.Http11Request;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public class HttpMethodDecoder implements DecodeChain<Http11Request> {

    private final HttpUriDecoder decoder = new HttpUriDecoder();

    @Override
    public DecodeChain<Http11Request> deocde(ByteBuffer byteBuffer, char[] cacheChars, AioSession<Http11Request> aioSession, Http11Request request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Consts.SP, cacheChars, true);
        if (length > 0) {
            String method = StringUtils.convertToString(cacheChars, length, StringUtils.String_CACHE_URL);
            request.setMethod(method);
            return decoder.deocde(byteBuffer, cacheChars, aioSession, request);
        } else {
            return this;
        }

    }
}
