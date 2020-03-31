/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Http11Response.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.WebSocketResponse;

import java.io.OutputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
public class WebSocketResponseImpl extends AbstractResponse implements WebSocketResponse {

    public WebSocketResponseImpl(WebSocketRequest request, OutputStream outputStream) {
        init(request, new WebSocketOutputStream(request, this, outputStream));
    }

    @Override
    public void sendTextMessage(String text) {
        System.out.println("发送字符串消息:" + text);
    }

    @Override
    public void sendBinaryMessage(byte[] bytes) {
        System.out.println("发送二进制消息:" + bytes);
    }
}
