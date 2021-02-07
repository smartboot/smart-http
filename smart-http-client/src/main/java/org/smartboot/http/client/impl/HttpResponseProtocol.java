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
import org.smartboot.http.common.utils.AttachKey;
import org.smartboot.http.common.utils.Attachment;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpResponseProtocol implements Protocol<Response> {

    public static final AttachKey<WebSocketResponseImpl> ATTACH_KEY_WS_REQ = AttachKey.valueOf("ws_response");
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
    public static final AttachKey<Response> ATTACH_KEY_RESPONSE = AttachKey.valueOf("response");

    private static final AttachKey<Decoder> ATTACH_KEY_DECODE_CHAIN = AttachKey.valueOf("decodeChain");

    private final HttpProtocolDecoder httpMethodDecoder = new HttpProtocolDecoder();

    private final WebSocketFrameDecoder wsFrameDecoder = new WebSocketFrameDecoder();

    @Override
    public Response decode(ByteBuffer buffer, AioSession session) {
        Attachment attachment = session.getAttachment();
        Response request = attachment.get(ATTACH_KEY_RESPONSE);
        Decoder decodeChain = attachment.get(ATTACH_KEY_DECODE_CHAIN);
        if (decodeChain == null) {
            decodeChain = httpMethodDecoder;
        }

        decodeChain = decodeChain.decode(buffer, session, request);

        if (decodeChain == HTTP_FINISH_DECODER || decodeChain == WS_HAND_SHARK_DECODER || decodeChain == WS_FRAME_DECODER) {
            if (decodeChain == HTTP_FINISH_DECODER) {
                attachment.remove(ATTACH_KEY_DECODE_CHAIN);
            } else {
                attachment.put(ATTACH_KEY_DECODE_CHAIN, wsFrameDecoder);
            }
            return request;
        }
        attachment.put(ATTACH_KEY_DECODE_CHAIN, decodeChain);
        if (buffer.remaining() == buffer.capacity()) {
            throw new RuntimeException("buffer is too small when decode " + decodeChain.getClass().getName() + " ," + request);
        }
        return null;
    }
}
