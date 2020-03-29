/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketRequest.java
 * Date: 2020-03-29
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class WebSocketRequest {
    private Http11Request httpRequest;

    public WebSocketRequest(Http11Request httpRequest) {
        this.httpRequest = httpRequest;
    }

    public Http11Request getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(Http11Request httpRequest) {
        this.httpRequest = httpRequest;
    }

    public enum WebsocketStatus {
        HandShake,
        DataFrame;
    }
}
