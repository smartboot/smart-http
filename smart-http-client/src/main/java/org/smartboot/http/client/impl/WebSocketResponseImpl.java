/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketResponseImpl.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.common.enums.WebsocketStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WebSocketResponseImpl extends AbstractResponse implements WebSocketResponse {
    public static final byte OPCODE_CONT = 0x0;
    public static final byte OPCODE_TEXT = 0x1;
    public static final byte OPCODE_BINARY = 0x2;
    public static final byte OPCODE_CLOSE = 0x8;
    public static final byte OPCODE_PING = 0x9;
    public static final byte OPCODE_PONG = 0xA;
    private final ByteArrayOutputStream payload = new ByteArrayOutputStream();
    private WebsocketStatus websocketStatus;
    private boolean frameFinalFlag;
    private boolean frameMasked;
    private int frameRsv;
    private int frameOpcode;

    public WebSocketResponseImpl(Response response) {
        init(response);
        this.websocketStatus = WebsocketStatus.HandShake;
    }

    public WebsocketStatus getWebsocketStatus() {
        return websocketStatus;
    }

    public void setWebsocketStatus(WebsocketStatus websocketStatus) {
        this.websocketStatus = websocketStatus;
    }

    public InputStream getInputStream() {
        throw new UnsupportedOperationException();
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

    public void setPayload(byte[] payload) {
        try {
            this.payload.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}