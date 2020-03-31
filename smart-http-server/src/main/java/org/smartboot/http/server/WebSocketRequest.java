/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketRequest.java
 * Date: 2020-03-29
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class WebSocketRequest extends AbstractRequest {
    public static final byte OPCODE_CONT = 0x0;
    public static final byte OPCODE_TEXT = 0x1;
    public static final byte OPCODE_BINARY = 0x2;
    public static final byte OPCODE_CLOSE = 0x8;
    public static final byte OPCODE_PING = 0x9;
    public static final byte OPCODE_PONG = 0xA;
    private WebsocketStatus websocketStatus;
    private boolean readingFrame = false;
    private long playLoadLen;
    private boolean frameFinalFlag;
    private boolean frameMasked;
    private int frameRsv;
    private int frameOpcode;

    private ByteArrayOutputStream playload = new ByteArrayOutputStream();
    private WebSocketResponseImpl response;

    public WebSocketRequest(BaseHttpRequest baseHttpRequest) {
        init(baseHttpRequest);
        this.websocketStatus = WebsocketStatus.HandShake;
        this.response = new WebSocketResponseImpl(this, baseHttpRequest.getAioSession().writeBuffer());
    }

    public final WebSocketResponseImpl getResponse() {
        return response;
    }

    public WebsocketStatus getWebsocketStatus() {
        return websocketStatus;
    }

    public void setWebsocketStatus(WebsocketStatus websocketStatus) {
        this.websocketStatus = websocketStatus;
    }

    public boolean isReadingFrame() {
        return readingFrame;
    }

    public void setReadingFrame(boolean readingFrame) {
        this.readingFrame = readingFrame;
    }

    public long getPlayLoadLen() {
        return playLoadLen;
    }

    public void setPlayLoadLen(long playLoadLen) {
        this.playLoadLen = playLoadLen;
    }


    public InputStream getInputStream() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void reset() {
        playload.reset();
//        super.reset();
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

    public byte[] getPlayload() {
        return playload.toByteArray();
    }

    public void setPlayload(byte[] playload) {
        try {
            this.playload.write(playload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public enum WebsocketStatus {
        HandShake,
        DataFrame;
    }
}
