/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpStatusCodeDecoder.java
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
class HttpStatusCodeDecoder implements HeaderDecoder {
    private final HttpStatusDescDecoder protocolDecoder = new HttpStatusDescDecoder();

    @Override
    public HeaderDecoder decode(ByteBuffer byteBuffer, AioSession aioSession, AbstractResponse response) {
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
