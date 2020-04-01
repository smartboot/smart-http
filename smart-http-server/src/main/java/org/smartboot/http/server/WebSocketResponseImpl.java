/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Http11Response.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.WebSocketResponse;
import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.server.decode.WebSocketFrameDecoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
public class WebSocketResponseImpl extends AbstractResponse implements WebSocketResponse {
    private static final int DEFAULT_MAX_FRAME_SIZE = 16384;

    public WebSocketResponseImpl(WebSocketRequestImpl request, OutputStream outputStream) {
        init(request, new WebSocketOutputStream(request, this, outputStream));
    }

    @Override
    public void sendTextMessage(String text) {
        RunLogger.getLogger().log(Level.INFO, "发送字符串消息:" + text);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try {
            send(WebSocketRequestImpl.OPCODE_TEXT, bytes);
        } catch (IOException e) {
            onError(e);
        }
    }

    @Override
    public void sendBinaryMessage(byte[] bytes) {
        System.out.println("发送二进制消息:" + bytes);
        try {
            send(WebSocketRequestImpl.OPCODE_BINARY, bytes);
        } catch (IOException e) {
            onError(e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void send(byte opCode, byte[] bytes) throws IOException {
        int maxlength;
        if (bytes.length < WebSocketFrameDecoder.PLAY_LOAD_126) {
            maxlength = 2 + bytes.length;
        } else if (bytes.length < DEFAULT_MAX_FRAME_SIZE) {
            maxlength = 4 + bytes.length;
        } else {
            maxlength = 4 + DEFAULT_MAX_FRAME_SIZE;
        }
        byte[] writBytes = new byte[maxlength];
        int offset = 0;

        while (offset < bytes.length) {
            int length = bytes.length - offset;
            if (length > DEFAULT_MAX_FRAME_SIZE) {
                length = DEFAULT_MAX_FRAME_SIZE;
            }
            byte firstByte = offset + length < bytes.length ? (byte) 0x00 : (byte) 0x80;
            if (offset == 0) {
                firstByte |= opCode;
            } else {
                firstByte |= WebSocketRequestImpl.OPCODE_CONT;
            }
            byte secondByte = length < WebSocketFrameDecoder.PLAY_LOAD_126 ? (byte) length : WebSocketFrameDecoder.PLAY_LOAD_126;
            writBytes[0] = firstByte;
            writBytes[1] = secondByte;
            if (secondByte == WebSocketFrameDecoder.PLAY_LOAD_126) {
                writBytes[2] = (byte) (length >> 8 & 0xff);
                writBytes[3] = (byte) (length & 0xff);
                System.arraycopy(bytes, offset, writBytes, 4, length);
            } else {
                System.arraycopy(bytes, offset, writBytes, 2, length);
            }
            System.out.println("write..");
            this.getOutputStream().write(writBytes, 0, length < WebSocketFrameDecoder.PLAY_LOAD_126 ? 2 + length : 4 + length);
            offset += length;
        }
    }

}
