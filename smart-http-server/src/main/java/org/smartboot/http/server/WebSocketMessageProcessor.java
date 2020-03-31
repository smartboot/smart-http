/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketMessageProcessor.java
 * Date: 2020-03-29
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.SHA1;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/29
 */
public class WebSocketMessageProcessor implements MessageProcessor<WebSocketRequest> {
    public static final String WEBSOCKET_13_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    @Override
    public void process(AioSession<WebSocketRequest> session, WebSocketRequest webSocketRequest) {
        if (webSocketRequest.getWebsocketStatus() == WebSocketRequest.WebsocketStatus.HandShake) {
            String key = webSocketRequest.getHttpRequest().getHeader(HttpHeaderConstant.Names.Sec_WebSocket_Key);
            String acceptSeed = key + WEBSOCKET_13_ACCEPT_GUID;
            byte[] sha1 = SHA1.encode(acceptSeed);
            String accept = Base64.getEncoder().encodeToString(sha1);
            Http11Response httpResponse = new Http11Response(webSocketRequest.getHttpRequest(), session.writeBuffer());
            httpResponse.setHttpStatus(HttpStatus.SWITCHING_PROTOCOLS);
            httpResponse.setHeader(HttpHeaderConstant.Names.UPGRADE, HttpHeaderConstant.Values.WEBSOCKET);
            httpResponse.setHeader(HttpHeaderConstant.Names.CONNECTION, HttpHeaderConstant.Values.UPGRADE);
            httpResponse.setHeader(HttpHeaderConstant.Names.Sec_WebSocket_Accept, accept);
            if (!httpResponse.isClosed()) {
                try {
                    httpResponse.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            webSocketRequest.setWebsocketStatus(WebSocketRequest.WebsocketStatus.DataFrame);
        } else {
            ByteBuffer byteBuffer = webSocketRequest.getFixedLengthFrameDecoder().getBuffer();
            byte[] b = new byte[byteBuffer.remaining()];
            byteBuffer.get(b);
            System.out.println(new String(b));
        }
    }

    @Override
    public void stateEvent(AioSession<WebSocketRequest> session, StateMachineEnum stateMachineEnum, Throwable throwable) {

    }
}
