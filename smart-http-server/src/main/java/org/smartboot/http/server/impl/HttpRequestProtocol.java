/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequestProtocol.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.decode.HttpMethodDecoder;
import org.smartboot.http.server.decode.HttpProxyContentDecoder;
import org.smartboot.http.server.decode.WebSocketFrameDecoder;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class HttpRequestProtocol implements Protocol<Request> {
    /**
     * 普通Http消息解码完成
     */
    public static final Decoder HTTP_FINISH_DECODER = (byteBuffer, aioSession, request) -> null;

    /**
     * Http 代理内容
     */
    public static final Decoder HTTP_PROXY_CONTENT = (byteBuffer, aioSession, request) -> null;
    /**
     * websocket握手消息
     */
    public static final Decoder WS_HANDSHAKE_DECODER = (byteBuffer, aioSession, request) -> null;
    /**
     * websocket负载数据读取成功
     */
    public static final Decoder WS_FRAME_DECODER = (byteBuffer, aioSession, request) -> null;
    private final WebSocketFrameDecoder wsFrameDecoder = new WebSocketFrameDecoder();
    private final HttpProxyContentDecoder httpProxyContentDecoder = new HttpProxyContentDecoder();
    private final HttpMethodDecoder httpMethodDecoder;

    public HttpRequestProtocol(HttpServerConfiguration configuration) {
        httpMethodDecoder = new HttpMethodDecoder(configuration);
    }

    @Override
    public Request decode(ByteBuffer buffer, AioSession session) {
        RequestAttachment attachment = session.getAttachment();
        Request request = attachment.getRequest();
        Decoder decodeChain = attachment.getDecoder();
        if (decodeChain == null) {
            decodeChain = httpMethodDecoder;
        }

        decodeChain = decodeChain.decode(buffer, session, request);

        if (decodeChain == HTTP_FINISH_DECODER) {
            attachment.setDecoder(null);
            return request;
        } else if (decodeChain == HTTP_PROXY_CONTENT) {
            attachment.setDecoder(httpProxyContentDecoder);
            return request;
        } else if (decodeChain == WS_HANDSHAKE_DECODER || decodeChain == WS_FRAME_DECODER) {
            attachment.setDecoder(wsFrameDecoder);
            return request;
        }
        attachment.setDecoder(decodeChain);
        if (buffer.remaining() == buffer.capacity()) {
            throw new RuntimeException("buffer is too small when decode " + decodeChain.getClass().getName() + " ," + request);
        }
        return null;
    }
}

