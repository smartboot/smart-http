/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestAttachment.java
 * Date: 2021-05-26
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.decode.Decoder;
import org.smartboot.http.common.utils.SmartDecoder;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/26
 */
public class ResponseAttachment {
    private final Response response;
    private Decoder decoder;
    private StringBuilder chunkBodyContent;
    private WebSocketRequestImpl webSocketRequest;
    private HttpRequestImpl httpRequest;

    private SmartDecoder bodyDecoder;

    public ResponseAttachment(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
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

    public StringBuilder getChunkBodyContent() {
        return chunkBodyContent;
    }

    public void setChunkBodyContent(StringBuilder chunkBodyContent) {
        this.chunkBodyContent = chunkBodyContent;
    }
}
