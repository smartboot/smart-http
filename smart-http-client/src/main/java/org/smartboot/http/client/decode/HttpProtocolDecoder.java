/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpProtocolDecoder.java
 * Date: 2021-07-15
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public class HttpProtocolDecoder implements HeaderDecoder {

    private final HttpStatusCodeDecoder decoder = new HttpStatusCodeDecoder();

    @Override
    public HeaderDecoder decode(ByteBuffer byteBuffer, AioSession aioSession, AbstractResponse request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.SP);
        if (length > 0) {
            request.setProtocol(StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length, StringUtils.String_CACHE_COMMON));
            return decoder.decode(byteBuffer, aioSession, request);
        } else {
            return this;
        }
    }
}
