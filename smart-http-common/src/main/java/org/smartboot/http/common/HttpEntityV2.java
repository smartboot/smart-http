/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpV2Entity.java
 * Date: 2018-01-23
 * Author: sandao
 */

package org.smartboot.http.common;

/**
 * Http消息体，兼容请求与响应
 *
 * @author 三刀 2018/06/02
 */
public class HttpEntityV2 {
    public final BufferRange verb = new BufferRange();
    public final BufferRange uri = new BufferRange();
    public final BufferRange protocol = new BufferRange();
    public final BufferRanges header = new BufferRanges();
    public int initPosition = 0;
    public State state = State.verb;
    private volatile int currentPosition = 0;
//    public ByteBuffer httpByte;

    public void rest() {
        verb.reset();
        uri.reset();
        protocol.reset();
        header.reset();
//        httpByte = null;
        initPosition = 0;
        setCurrentPosition(0);
        state = State.verb;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    public String toString() {
        return "HttpEntityV2" + hashCode() + "{" +
                "initPosition=" + initPosition +
                ", currentPosition=" + currentPosition +
                '}';
    }
}
