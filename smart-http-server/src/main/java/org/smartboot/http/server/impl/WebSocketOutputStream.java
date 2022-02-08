/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class WebSocketOutputStream extends AbstractOutputStream {

    public WebSocketOutputStream(WebSocketRequestImpl webSocketRequest, WebSocketResponseImpl response, Request request) {
        super(webSocketRequest, response, request);
        super.chunked = false;
    }

    /**
     * 输出Http消息头
     */
    protected void writeHeader() throws IOException {
        if (committed) {
            return;
        }
        //转换Cookie
        convertCookieToHeader();

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        byte[] headBytes = getBytes(request.getProtocol() + " " + response.getHttpStatus() + " " + response.getReasonPhrase() + "\r\n"
                + HeaderNameEnum.CONTENT_TYPE.getName() + ":" + response.getContentType() + (hasHeader() ? "\r\n" : "\r\n\r\n"));
        writeBuffer.write(headBytes);
        //输出Header部分
        writeHeaders();

        committed = true;
    }

}
