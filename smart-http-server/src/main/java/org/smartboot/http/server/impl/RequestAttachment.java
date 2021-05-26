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

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/26
 */
public class RequestAttachment {
    private final Request request;
    private Decoder decoder;
    private WebSocketRequestImpl webSocketRequest;
    private HttpRequestImpl httpRequest;

    private SmartDecoder bodyDecoder;

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
}
