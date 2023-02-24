/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.SHA1;
import org.smartboot.http.server.decode.websocket.BasicFrameDecoder;
import org.smartboot.http.server.decode.websocket.BodyAttachment;
import org.smartboot.http.server.decode.websocket.Decoder;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.http.server.impl.WebSocketResponseImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * WebSocket消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class WebSocketHandler implements ServerHandler<WebSocketRequest, WebSocketResponse> {
    public static final String WEBSOCKET_13_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final Decoder basicFrameDecoder = new BasicFrameDecoder();

    public static final Decoder PAYLOAD_FINISH = new Decoder() {
        @Override
        public Decoder decode(ByteBuffer byteBuffer, WebSocketRequestImpl request) {
            return this;
        }
    };

    public void willHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {

    }

    @Override
    public final void onHeaderComplete(Request request) throws IOException {
        WebSocketRequestImpl webSocketRequest = request.newWebsocketRequest();
        WebSocketResponseImpl response = webSocketRequest.getResponse();
        willHeaderComplete(webSocketRequest, response);
        String key = request.getHeader(HeaderNameEnum.Sec_WebSocket_Key.getName());
        String acceptSeed = key + WEBSOCKET_13_ACCEPT_GUID;
        byte[] sha1 = SHA1.encode(acceptSeed);
        String accept = Base64.getEncoder().encodeToString(sha1);
        response.setHttpStatus(HttpStatus.SWITCHING_PROTOCOLS);
        response.setHeader(HeaderNameEnum.UPGRADE.getName(), HeaderValueEnum.WEBSOCKET.getName());
        response.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.UPGRADE.getName());
        response.setHeader(HeaderNameEnum.Sec_WebSocket_Accept.getName(), accept);
        OutputStream outputStream = response.getOutputStream();
        outputStream.flush();

        BodyAttachment attachment = new BodyAttachment();
        attachment.setDecoder(basicFrameDecoder);
        webSocketRequest.setAttachment(attachment);
        whenHeaderComplete(webSocketRequest, response);
    }

    public void whenHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {

    }

    @Override
    public boolean onBodyStream(ByteBuffer byteBuffer, Request request) {
        BodyAttachment attachment = request.newWebsocketRequest().getAttachment();
        Decoder decoder = attachment.getDecoder().decode(byteBuffer, request.newWebsocketRequest());
        if (decoder == PAYLOAD_FINISH) {
            attachment.setDecoder(basicFrameDecoder);
            return true;
        } else {
            attachment.setDecoder(decoder);
            return false;
        }
    }

}
