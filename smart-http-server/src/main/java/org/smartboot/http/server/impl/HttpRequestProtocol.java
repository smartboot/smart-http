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
import org.smartboot.socket.DecoderException;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class HttpRequestProtocol implements Protocol<Request> {
    public static final Decoder BODY_READY_DECODER = (byteBuffer, response) -> null;
    public static final Decoder BODY_CONTINUE_DECODER = (byteBuffer, response) -> null;
    /**
     * websocket负载数据读取成功
     */
    private final HttpMethodDecoder httpMethodDecoder;

    private final HttpServerConfiguration configuration;

    public HttpRequestProtocol(HttpServerConfiguration configuration) {
        httpMethodDecoder = new HttpMethodDecoder(configuration);
        this.configuration = configuration;
    }

    @Override
    public Request decode(ByteBuffer buffer, AioSession session) {
        Request request = session.getAttachment();
        Decoder decodeChain = request.getDecoder();
        if (decodeChain == null) {
            decodeChain = httpMethodDecoder;
        }
        if (configuration.getHttpIdleTimeout() > 0 || configuration.getWsIdleTimeout() > 0) {
            request.setLatestIo(System.currentTimeMillis());
        }

        // 数据还未就绪，继续读
        if (decodeChain == BODY_CONTINUE_DECODER) {
            request.setDecoder(BODY_READY_DECODER);
            return null;
        } else if (decodeChain == BODY_READY_DECODER) {
            return request;
        }

        int p = buffer.position();
        decodeChain = decodeChain.decode(buffer, request);
        request.decodeSize(buffer.position() - p);
        request.setDecoder(decodeChain);
        if (decodeChain == BODY_READY_DECODER) {
            return request;
        }
        if (buffer.remaining() == buffer.capacity()) {
            throw new DecoderException("buffer is too small when decode " + decodeChain.getClass().getName() + " ," + request);
        }
        return null;
    }
}

