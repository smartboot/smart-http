/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: UriDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpUriDecoder extends AbstractDecoder {
    private final HttpUriQueryDecoder uriQueryDecoder = new HttpUriQueryDecoder(getConfiguration());
    private final HttpProtocolDecoder protocolDecoder = new HttpProtocolDecoder(getConfiguration());

    public HttpUriDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request request) {
        int length = scanURI(byteBuffer);
        if (length > 0) {
            String uri = StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length, StringUtils.String_CACHE_URI, true);
            request.setUri(uri);
            switch (byteBuffer.get(byteBuffer.position() - 1)) {
                case Constant.SP:
                    return protocolDecoder.decode(byteBuffer, aioSession, request);
                case '?':
                    return uriQueryDecoder.decode(byteBuffer, aioSession, request);
                default:
                    throw new HttpException(HttpStatus.BAD_REQUEST);
            }
        } else {
            return this;
        }
    }

    private int scanURI(ByteBuffer buffer) {
        StringUtils.trimBuffer(buffer);
        int position = buffer.position() + buffer.arrayOffset();
        int limit = buffer.limit() + buffer.arrayOffset();
        byte[] data = buffer.array();
        while (position < limit) {
            byte b = data[position++];
            if (b == ' ' || b == '?') {
                int length = position - buffer.arrayOffset() - buffer.position() - 1;
                buffer.position(position - buffer.arrayOffset());
                return length;
            }
        }
        return -1;
    }
}
