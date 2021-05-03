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
class HttpProtocolDecoder implements Decoder {

    private final HttpHeaderDecoder decoder = new HttpHeaderDecoder();
    private final IgnoreHeaderDecoder ignoreHeaderDecoder = new IgnoreHeaderDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.LF);
        if (length > 0) {
//            if (byteBuffer.get(byteBuffer.position() - 2) != Constant.CR) {
//                throw new HttpException(HttpStatus.BAD_REQUEST);
//            }
            String protocol = StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length - 1, StringUtils.String_CACHE_HTTP_PROTOCOL);
            request.setProtocol(protocol);
            return decoder.decode(byteBuffer, aioSession, request);
//            return ignoreHeaderDecoder.decode(byteBuffer, aioSession, request);
        } else {
            return this;
        }

    }
}
