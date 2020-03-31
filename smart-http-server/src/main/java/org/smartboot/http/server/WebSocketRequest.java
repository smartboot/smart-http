/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketRequest.java
 * Date: 2020-03-29
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.utils.FixedLengthFrameDecoder;

import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class WebSocketRequest {
    public static final int BUFFER_LIMIT = 1024 * 64;
    private Http11Request httpRequest;


    private WebsocketStatus websocketStatus;
    private boolean readingFrame = false;
    private long playLoadLen;
    private FixedLengthFrameDecoder fixedLengthFrameDecoder;
    private boolean frameFinalFlag;
    private boolean frameMasked;
    private int frameRsv;
    private int frameOpcode;

    private InputStream inputStream;


    public WebSocketRequest(Http11Request httpRequest) {
        this.httpRequest = httpRequest;
        this.websocketStatus = WebsocketStatus.HandShake;
    }

    public Http11Request getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(Http11Request httpRequest) {
        this.httpRequest = httpRequest;
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
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public FixedLengthFrameDecoder getFixedLengthFrameDecoder() {
        return fixedLengthFrameDecoder;
    }

    public void setFixedLengthFrameDecoder(FixedLengthFrameDecoder fixedLengthFrameDecoder) {
        this.fixedLengthFrameDecoder = fixedLengthFrameDecoder;
    }

    public enum WebsocketStatus {
        HandShake,
        DataFrame;
    }
}
