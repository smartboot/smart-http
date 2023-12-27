/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpResponseProtocol.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.client.decode.HeaderDecoder;
import org.smartboot.http.client.decode.HttpProtocolDecoder;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpResponseProtocol implements Protocol<AbstractResponse> {
    public static HttpResponseProtocol INSTANCE = new HttpResponseProtocol();
    public static final HeaderDecoder BODY_READY_DECODER = (byteBuffer, aioSession, response) -> null;
    public static final HeaderDecoder BODY_CONTINUE_DECODER = (byteBuffer, aioSession, response) -> null;

    private final HttpProtocolDecoder httpMethodDecoder = new HttpProtocolDecoder();

    @Override
    public AbstractResponse decode(ByteBuffer buffer, AioSession session) {
        ResponseAttachment attachment = session.getAttachment();
        HeaderDecoder decodeChain = attachment.getDecoder();
        if (decodeChain == null) {
            if (!attachment.isWs()) {
                attachment.setResponse(new HttpResponseImpl(session));
            }
            decodeChain = httpMethodDecoder;
        }
        AbstractResponse response = attachment.getResponse();

        // 数据还未就绪，继续读
        if (decodeChain == BODY_CONTINUE_DECODER) {
            attachment.setDecoder(BODY_READY_DECODER);
            return null;
        } else if (decodeChain == BODY_READY_DECODER) {
            return response;
        }

        decodeChain = decodeChain.decode(buffer, session, response);
        attachment.setDecoder(decodeChain);
        // 响应式流
        if (decodeChain == BODY_READY_DECODER) {
            return response;
        }
        if (buffer.remaining() == buffer.capacity()) {
            throw new RuntimeException("buffer is too small when decode " + decodeChain.getClass().getName() + " ," + response);
        }
        return null;
    }
}
