/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequestProtocol.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.server.decode.Decoder;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class HttpRequestProtocol implements Protocol<Request> {
    public static final Decoder BODY_READY_DECODER = (byteBuffer, response) -> null;
    public static final Decoder BODY_CONTINUE_DECODER = (byteBuffer, response) -> null;

    @Override
    public Request decode(ByteBuffer buffer, AioSession session) {
        throw new IllegalStateException();
    }
}

