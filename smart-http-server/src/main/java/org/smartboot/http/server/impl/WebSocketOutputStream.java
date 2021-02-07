/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class WebSocketOutputStream extends AbstractOutputStream {

    public WebSocketOutputStream(WebSocketRequestImpl request, WebSocketResponseImpl response, WriteBuffer writeBuffer) {
        super(request, response, writeBuffer);
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
        if (response.getHttpStatus() == null) {
            response.setHttpStatus(HttpStatus.SWITCHING_PROTOCOLS);
        }
        String contentType = response.getContentType();
        if (contentType == null) {
            contentType = HttpHeaderConstant.Values.DEFAULT_CONTENT_TYPE;
        }

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        writeBuffer.write(getBytes(request.getProtocol() + response.getHttpStatus().getHttpStatusLine() + "\r\n"
                + HttpHeaderConstant.Names.CONTENT_TYPE + ":" + contentType));

        //输出Header部分
        if (response.getHeaders() != null) {
            for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
                HeaderValue headerValue = entry.getValue();
                while (headerValue != null) {
                    writeBuffer.write(getHeaderNameBytes(entry.getKey()));
                    writeBuffer.write(getBytes(headerValue.getValue()));
                    headerValue = headerValue.getNextValue();
                }
            }
        }

        /**
         * RFC2616 3.3.1
         * 只能用 RFC 1123 里定义的日期格式来填充头域 (header field)的值里用到 HTTP-date 的地方
         */
        flushDate();
        writeBuffer.write(date);
        committed = true;
    }

}
