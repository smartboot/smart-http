/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketOutputStream.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class WebSocketOutputStream extends AbstractOutputStream {

    public WebSocketOutputStream(WebSocketRequestImpl request, WriteBuffer writeBuffer) {
        super(request, writeBuffer);
        super.chunked = false;
    }

    /**
     * 输出Http消息头
     *
     * @throws IOException
     */
    protected void writeHead() throws IOException {
        if (committed) {
            return;
        }
        String contentType = request.getContentType();
        if (contentType == null) {
            contentType = HeaderValueEnum.DEFAULT_CONTENT_TYPE.getName();
        }

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        String headLine = request.getMethod() + " " + request.getUri() + " " + request.getProtocol();
        writeBuffer.write(getBytes(headLine));
        writeBuffer.write(Constant.CRLF);
        writeBuffer.write("1:1".getBytes());

        //输出Header部分
        if (request.getHeaders() != null) {
            for (Map.Entry<String, HeaderValue> entry : request.getHeaders().entrySet()) {
                HeaderValue headerValue = entry.getValue();
                while (headerValue != null) {
                    writeBuffer.write(getHeaderNameBytes(entry.getKey()));
                    writeBuffer.write(getBytes(headerValue.getValue()));
                    writeBuffer.write(Constant.CRLF);
                    headerValue = headerValue.getNextValue();
                }
            }
        }
        writeBuffer.write(Constant.CRLF);

        committed = true;
    }

}
