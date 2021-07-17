/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpResponseProtocol.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.decode.HeaderDecoder;
import org.smartboot.http.client.decode.HttpProtocolDecoder;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpResponseProtocol implements Protocol<Response> {
    
    public static final HeaderDecoder REACTIVE_STREAM_DECODER = (byteBuffer, aioSession, response) -> null;

    private final HttpProtocolDecoder httpMethodDecoder = new HttpProtocolDecoder();

    @Override
    public Response decode(ByteBuffer buffer, AioSession session) {
        ResponseAttachment attachment = session.getAttachment();
        Response request = attachment.getResponse();
        HeaderDecoder decodeChain = attachment.getDecoder();
        if (decodeChain == null) {
            decodeChain = httpMethodDecoder;
        }
        if (decodeChain == REACTIVE_STREAM_DECODER) {
            return request;
        }
        decodeChain = decodeChain.decode(buffer, session, request);
        attachment.setDecoder(decodeChain);
        // 响应式流
        if (decodeChain == REACTIVE_STREAM_DECODER) {
            return request;
        }
        if (buffer.remaining() == buffer.capacity()) {
            throw new RuntimeException("buffer is too small when decode " + decodeChain.getClass().getName() + " ," + request);
        }
        return null;
    }
}
