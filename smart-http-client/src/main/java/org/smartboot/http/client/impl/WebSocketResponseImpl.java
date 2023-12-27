/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Response.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.client.WebSocketResponse;
import org.smartboot.http.common.codec.websocket.WebSocket;
import org.smartboot.http.common.enums.DecodePartEnum;
import org.smartboot.http.common.utils.WebSocketUtil;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.util.Attachment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class WebSocketResponseImpl extends AbstractResponse implements WebSocket, WebSocketResponse {
    private final ByteArrayOutputStream payload = new ByteArrayOutputStream();
    private boolean frameFinalFlag;
    private boolean frameMasked;
    private int frameRsv;
    private int frameOpcode;

    /**
     * payload长度
     */
    private long payloadLength;

    private byte[] maskingKey;
    private Attachment attachment = new Attachment();

    public WebSocketResponseImpl(AioSession session) {
        super(session);
    }


    public void reset() {
        setDecodePartEnum(DecodePartEnum.BODY);
        if (frameOpcode != WebSocketUtil.OPCODE_CONTINUE) {
            payload.reset();
        }
    }

    public boolean isFrameFinalFlag() {
        return frameFinalFlag;
    }

    public void setFrameFinalFlag(boolean frameFinalFlag) {
        this.frameFinalFlag = frameFinalFlag;
    }

    public boolean isFrameMasked() {
        return frameMasked;
    }

    public void setFrameMasked(boolean frameMasked) {
        this.frameMasked = frameMasked;
    }

    public int getFrameRsv() {
        return frameRsv;
    }

    public void setFrameRsv(int frameRsv) {
        this.frameRsv = frameRsv;
    }

    public int getFrameOpcode() {
        return frameOpcode;
    }

    public void setFrameOpcode(int frameOpcode) {
        this.frameOpcode = frameOpcode;
    }

    public byte[] getPayload() {
        return payload.toByteArray();
    }

    public long getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(long payloadLength) {
        this.payloadLength = payloadLength;
    }

    public byte[] getMaskingKey() {
        return maskingKey;
    }

    public void setMaskingKey(byte[] maskingKey) {
        this.maskingKey = maskingKey;
    }

    public void setPayload(byte[] payload) {
        try {
            this.payload.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Attachment getAttachment() {
        return attachment;
    }
}
