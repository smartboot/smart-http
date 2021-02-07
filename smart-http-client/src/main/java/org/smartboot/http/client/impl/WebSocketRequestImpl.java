/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketRequestImpl.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
public class WebSocketRequestImpl extends AbstractRequest implements WebSocketRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketRequestImpl.class);

    public WebSocketRequestImpl(WriteBuffer outputStream) {
        init(new WebSocketOutputStream(this, outputStream));
    }

    @Override
    public void sendTextMessage(String text) {
        LOGGER.info("发送字符串消息:{}", text);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try {
            send(WebSocketResponseImpl.OPCODE_TEXT, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendBinaryMessage(byte[] bytes) {
        LOGGER.info("发送二进制消息:{}", bytes);
        try {
            send(WebSocketResponseImpl.OPCODE_BINARY, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flush() {
        try {
            getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void send(byte opCode, byte[] bytes) throws IOException {
        int maxlength;
        if (bytes.length < Constant.WS_PLAY_LOAD_126) {
            maxlength = 2 + bytes.length;
        } else if (bytes.length < Constant.WS_DEFAULT_MAX_FRAME_SIZE) {
            maxlength = 4 + bytes.length;
        } else {
            maxlength = 4 + Constant.WS_DEFAULT_MAX_FRAME_SIZE;
        }
        byte[] writBytes = new byte[maxlength];
        int offset = 0;

        while (offset < bytes.length) {
            int length = bytes.length - offset;
            if (length > Constant.WS_DEFAULT_MAX_FRAME_SIZE) {
                length = Constant.WS_DEFAULT_MAX_FRAME_SIZE;
            }
            byte firstByte = offset + length < bytes.length ? (byte) 0x00 : (byte) 0x80;
            if (offset == 0) {
                firstByte |= opCode;
            } else {
                firstByte |= WebSocketResponseImpl.OPCODE_CONT;
            }
            byte secondByte = length < Constant.WS_PLAY_LOAD_126 ? (byte) length : Constant.WS_PLAY_LOAD_126;
            writBytes[0] = firstByte;
            writBytes[1] = secondByte;
            if (secondByte == Constant.WS_PLAY_LOAD_126) {
                writBytes[2] = (byte) (length >> 8 & 0xff);
                writBytes[3] = (byte) (length & 0xff);
                System.arraycopy(bytes, offset, writBytes, 4, length);
            } else {
                System.arraycopy(bytes, offset, writBytes, 2, length);
            }
            this.getOutputStream().write(writBytes, 0, length < Constant.WS_PLAY_LOAD_126 ? 2 + length : 4 + length);
            offset += length;
        }
    }

}
