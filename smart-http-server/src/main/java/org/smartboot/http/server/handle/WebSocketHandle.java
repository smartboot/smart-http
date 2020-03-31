/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketHandle.java
 * Date: 2020-03-31
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handle;

import org.smartboot.http.HttpResponse;
import org.smartboot.http.WebSocketResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.WebSocketRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/31
 */
public class WebSocketHandle extends HttpHandle<WebSocketRequest> {


    @Override
    public final void doHandle(WebSocketRequest request, HttpResponse response) throws IOException {
        switch (request.getWebsocketStatus()) {
            case HandShake:
                finishHandshark(request, request.getResponse());
                break;
            case DataFrame: {
                switch (request.getFrameOpcode()) {
                    case WebSocketRequest.OPCODE_TEXT:
                        handleTextMessage(request, request.getResponse(), new String(request.getPlayload(), StandardCharsets.UTF_8));
                        break;
                    case WebSocketRequest.OPCODE_BINARY:
                        handleBinaryMessage(request, request.getResponse(), request.getPlayload());
                        break;
                    case WebSocketRequest.OPCODE_CLOSE:
                        break;
                    case WebSocketRequest.OPCODE_PING:
                        break;
                    case WebSocketRequest.OPCODE_PONG:
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            break;
            default:
                throw new HttpException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 握手成功
     *
     * @param request
     * @param webSocketResponse
     */
    public void finishHandshark(WebSocketRequest request, WebSocketResponse webSocketResponse) {

    }

    /**
     * 处理字符串请求消息
     *
     * @param request
     * @param webSocketResponse
     * @param data
     */
    public void handleTextMessage(WebSocketRequest request, WebSocketResponse webSocketResponse, String data) {
        System.out.println(data);
    }

    /**
     * 处理二进制请求消息
     *
     * @param request
     * @param webSocketResponse
     * @param data
     */
    public void handleBinaryMessage(WebSocketRequest request, WebSocketResponse webSocketResponse, byte[] data) {
        System.out.println(data);
    }

}
