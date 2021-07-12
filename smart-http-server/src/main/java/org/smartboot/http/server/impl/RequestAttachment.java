/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestAttachment.java
 * Date: 2021-05-26
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.server.decode.Decoder;

import java.nio.ByteBuffer;

/**
 * Http/Ws 请求解码附件对象
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/26
 */
public class RequestAttachment {
    /**
     * 请求对象
     */
    private final Request request;
    /**
     * 当前使用的解码器
     */
    private Decoder decoder;
    /**
     * websocket 请求对象
     */
    private WebSocketRequestImpl webSocketRequest;
    /**
     * http 请求对象
     */
    private HttpRequestImpl httpRequest;

    /**
     * http body 解码器
     */
    private SmartDecoder bodyDecoder;

    /**
     * read缓冲区
     */
    private ByteBuffer readBuffer;

    public RequestAttachment(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    public WebSocketRequestImpl getWebSocketRequest() {
        return webSocketRequest;
    }

    public void setWebSocketRequest(WebSocketRequestImpl webSocketRequest) {
        this.webSocketRequest = webSocketRequest;
    }

    public HttpRequestImpl getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequestImpl httpRequest) {
        this.httpRequest = httpRequest;
    }

    public SmartDecoder getBodyDecoder() {
        return bodyDecoder;
    }

    public void setBodyDecoder(SmartDecoder bodyDecoder) {
        this.bodyDecoder = bodyDecoder;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public void setReadBuffer(ByteBuffer readBuffer) {
        this.readBuffer = readBuffer;
    }
}
