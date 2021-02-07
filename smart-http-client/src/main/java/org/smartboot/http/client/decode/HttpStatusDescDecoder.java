/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.impl.Response;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpStatusDescDecoder implements Decoder {

    private final HttpHeaderDecoder decoder = new HttpHeaderDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.LF);
        if (length > 0) {
            String protocol = StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length - 1, StringUtils.String_CACHE_URL);
            request.setStatusDesc(protocol);
            return decoder.decode(byteBuffer, aioSession, request);
        } else {
            return this;
        }

    }
}
