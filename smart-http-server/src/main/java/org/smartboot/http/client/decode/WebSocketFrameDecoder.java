/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.HttpResponseProtocol;
import org.smartboot.http.client.Response;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.logging.Logger;
import org.smartboot.http.logging.LoggerFactory;
import org.smartboot.http.server.HttpRequestProtocol;
import org.smartboot.http.server.WebSocketRequestImpl;
import org.smartboot.http.utils.Attachment;
import org.smartboot.http.utils.Constant;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public class WebSocketFrameDecoder implements Decoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketFrameDecoder.class);

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response request) {
        if (byteBuffer.remaining() < 2) {
            return this;
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
                return this;
            }
            length = Short.toUnsignedInt(byteBuffer.getShort());
        }
        if (length > Constant.WS_DEFAULT_MAX_FRAME_SIZE) {
            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        if (byteBuffer.remaining() < (mask ? length + 4 : length)) {
            byteBuffer.reset();
            return this;
        }

        boolean fin = (first & 0x80) != 0;
        int rsv = (first & 0x70) >> 4;
        int opcode = first & 0x0f;

        Attachment attachment = aioSession.getAttachment();
        WebSocketRequestImpl webSocketRequest = attachment.get(HttpRequestProtocol.ATTACH_KEY_WS_REQ);
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
        LOGGER.info("read ws data...");
        webSocketRequest.setPayload(payload);

        return fin ? HttpResponseProtocol.WS_FRAME_DECODER : this;
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
