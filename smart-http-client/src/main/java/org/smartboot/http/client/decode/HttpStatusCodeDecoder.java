/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: UriDecoder.java
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
class HttpStatusCodeDecoder implements Decoder {
    private final HttpStatusDescDecoder protocolDecoder = new HttpStatusDescDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.SP);
        if (length > 0) {
            int statusCode = StringUtils.convertToInteger(byteBuffer, byteBuffer.position() - length - 1, length, StringUtils.INTEGER_CACHE_HTTP_STATUS_CODE);
            response.setStatus(statusCode);
            return protocolDecoder.decode(byteBuffer, aioSession, response);
        } else {
            return this;
        }
    }


}
