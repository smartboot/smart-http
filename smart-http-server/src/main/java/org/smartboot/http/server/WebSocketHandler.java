/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.Handler;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.SHA1;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.http.server.impl.WebSocketResponseImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

/**
 * WebSocket消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class WebSocketHandler extends Handler<WebSocketRequest, WebSocketResponse, Request> {
    public static final String WEBSOCKET_13_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        super.onHeaderComplete(request);
        WebSocketResponseImpl response = request.newWebsocketRequest().getResponse();
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
    }

    @Override
    public int onBodyStream(ByteBuffer byteBuffer, Request request) {
        if (byteBuffer.remaining() < 2) {
            return BODY_CONTINUE;
        }
        byteBuffer.mark();
        int first = byteBuffer.get();
        int second = byteBuffer.get();
        boolean mask = (second & 0x80) != 0;
        int length = second & 0x7F;
        if (length == Constant.WS_PLAY_LOAD_127) {
            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        if (length == Constant.WS_PLAY_LOAD_126) {
            if (byteBuffer.remaining() < Short.BYTES) {
                byteBuffer.reset();
                return BODY_CONTINUE;
            }
            length = Short.toUnsignedInt(byteBuffer.getShort());
        }
        if (length > Constant.WS_DEFAULT_MAX_FRAME_SIZE) {
            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        if (byteBuffer.remaining() < (mask ? length + 4 : length)) {
            byteBuffer.reset();
            return BODY_CONTINUE;
        }

        boolean fin = (first & 0x80) != 0;
        int rsv = (first & 0x70) >> 4;
        int opcode = first & 0x0f;
        WebSocketRequestImpl webSocketRequest = request.newWebsocketRequest();
        webSocketRequest.setFrameFinalFlag(fin);
        webSocketRequest.setFrameRsv(rsv);
        webSocketRequest.setFrameOpcode(opcode);
        webSocketRequest.setFrameMasked(mask);

        if (mask) {
            byte[] maskingKey = new byte[4];
            byteBuffer.get(maskingKey);
            unmask(byteBuffer, maskingKey, length);
        }
        byte[] payload = new byte[length];
        byteBuffer.get(payload);
        webSocketRequest.setPayload(payload);

        return fin ? BODY_FINISH : BODY_CONTINUE;
    }

    private void unmask(ByteBuffer frame, byte[] maskingKey, int length) {
        int i = frame.position();
        int end = i + length;

        ByteOrder order = frame.order();

        // Remark: & 0xFF is necessary because Java will do signed expansion from
        // byte to int which we don't want.
        int intMask = ((maskingKey[0] & 0xFF) << 24)
                | ((maskingKey[1] & 0xFF) << 16)
                | ((maskingKey[2] & 0xFF) << 8)
                | (maskingKey[3] & 0xFF);

        // If the byte order of our buffers it little endian we have to bring our mask
        // into the same format, because getInt() and writeInt() will use a reversed byte order
        if (order == ByteOrder.LITTLE_ENDIAN) {
            intMask = Integer.reverseBytes(intMask);
        }

        for (; i + 3 < end; i += 4) {
            int unmasked = frame.getInt(i) ^ intMask;
            frame.putInt(i, unmasked);
        }
        int j = i;
        for (; i < end; i++) {
            frame.put(i, (byte) (frame.get(i) ^ maskingKey[(i - j) % 4]));
        }
    }
}
