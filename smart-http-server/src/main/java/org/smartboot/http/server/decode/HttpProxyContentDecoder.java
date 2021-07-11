/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpProxyContentDecoder.java
 * Date: 2021-07-10
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.RequestAttachment;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;


/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/10
 */
public class HttpProxyContentDecoder implements Decoder {
    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request request) {
        if (!byteBuffer.hasRemaining()) {
            return this;
        }
        RequestAttachment attachment = aioSession.getAttachment();
        attachment.setProxyContent(byteBuffer);
        return HttpRequestProtocol.HTTP_PROXY_CONTENT;
    }
}
