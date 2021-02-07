/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpUriQueryDecoder implements Decoder {

    private final HttpProtocolDecoder decoder = new HttpProtocolDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.SP);
        if (length > 0) {
            String query = StringUtils.convertToString(byteBuffer, byteBuffer.position() - 1 - length, length, StringUtils.String_CACHE_URL);
            request.setQueryString(query);
            return decoder.decode(byteBuffer, aioSession, request);
        } else {
            return this;
        }

    }
}
