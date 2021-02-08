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
public class HttpProtocolDecoder implements Decoder {

    private final HttpStatusCodeDecoder decoder = new HttpStatusCodeDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.SP);
        if (length > 0) {
            String method = StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length, StringUtils.String_CACHE_HTTP_METHOD);
            request.setProtocol(method);
            return decoder.decode(byteBuffer, aioSession, request);
        } else {
            return this;
        }
    }
}
