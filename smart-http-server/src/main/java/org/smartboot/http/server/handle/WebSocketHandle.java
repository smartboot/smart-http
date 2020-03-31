/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketHandle.java
 * Date: 2020-03-31
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handle;

import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/31
 */
public class WebSocketHandle extends HttpHandle<WebSocketRequest> {
    @Override
    public final void doHandle(WebSocketRequest request, HttpResponse response) throws IOException {
        switch (request.getWebsocketStatus()) {
            case HandShake:
                onHandShark(request, request.getResponse());
                break;
            case DataFrame:
                onDataFrame(request, request.getResponse());
                break;
            default:
                throw new HttpException(HttpStatus.BAD_REQUEST);
        }
    }

    public void onHandShark(WebSocketRequest request, WebSocketResponse webSocketResponse) {

    }

    public void onDataFrame(WebSocketRequest request, WebSocketResponse webSocketResponse) {

    }
}
