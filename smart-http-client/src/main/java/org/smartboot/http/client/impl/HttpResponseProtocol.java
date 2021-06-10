/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpResponseProtocol.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.decode.Decoder;
import org.smartboot.http.client.decode.HttpProtocolDecoder;
import org.smartboot.http.client.decode.WebSocketFrameDecoder;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpResponseProtocol implements Protocol<Response> {

    /**
     * 普通Http消息解码完成
     */
    public static final Decoder HTTP_FINISH_DECODER = (byteBuffer, aioSession, response) -> null;
    /**
     * websocket握手消息
     */
    public static final Decoder WS_HAND_SHARK_DECODER = (byteBuffer, aioSession, response) -> null;
    /**
     * websocket负载数据读取成功
     */
    public static final Decoder WS_FRAME_DECODER = (byteBuffer, aioSession, response) -> null;

    private final HttpProtocolDecoder httpMethodDecoder = new HttpProtocolDecoder();

    private final WebSocketFrameDecoder wsFrameDecoder = new WebSocketFrameDecoder();

    @Override
    public Response decode(ByteBuffer buffer, AioSession session) {
        ResponseAttachment attachment = session.getAttachment();
        Response request = attachment.getResponse();
        Decoder decodeChain = attachment.getDecoder();
        if (decodeChain == null) {
            decodeChain = httpMethodDecoder;
        }

        decodeChain = decodeChain.decode(buffer, session, request);

        if (decodeChain == HTTP_FINISH_DECODER || decodeChain == WS_HAND_SHARK_DECODER || decodeChain == WS_FRAME_DECODER) {
            if (decodeChain == HTTP_FINISH_DECODER) {
                attachment.setDecoder(null);
            } else {
                attachment.setDecoder(wsFrameDecoder);
            }
            return request;
        }
        attachment.setDecoder(decodeChain);
        if (buffer.remaining() == buffer.capacity()) {
            throw new RuntimeException("buffer is too small when decode " + decodeChain.getClass().getName() + " ," + request);
        }
        return null;
    }
}
