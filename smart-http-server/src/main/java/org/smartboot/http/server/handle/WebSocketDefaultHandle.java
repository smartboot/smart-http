/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketHandle.java
 * Date: 2020-03-31
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handle;

import org.smartboot.http.WebSocketRequest;
import org.smartboot.http.WebSocketResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.WebSocketRequestImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * @author 三刀
 * @version V1.0 , 2020/3/31
 */
public class WebSocketDefaultHandle extends WebSocketHandle {

    @Override
    public final void doHandle(WebSocketRequest request, WebSocketResponse response) throws IOException {
        switch (request.getWebsocketStatus()) {
            case HandShake:
                finishHandshark(request, response);
                break;
            case DataFrame: {
                switch (request.getFrameOpcode()) {
                    case WebSocketRequestImpl.OPCODE_TEXT:
                        handleTextMessage(request, response, new String(request.getPlayload(), StandardCharsets.UTF_8));
                        break;
                    case WebSocketRequestImpl.OPCODE_BINARY:
                        handleBinaryMessage(request, response, request.getPlayload());
                        break;
                    case WebSocketRequestImpl.OPCODE_CLOSE:
                        System.out.println("close:" + new String(request.getPlayload()));
//                        request.getRequest().getAioSession().close(false);
                        break;
                    case WebSocketRequestImpl.OPCODE_PING:
                        break;
                    case WebSocketRequestImpl.OPCODE_PONG:
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
