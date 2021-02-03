/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends AbstractOutputStream {
    public HttpOutputStream(HttpRequestImpl request, WriteBuffer writeBuffer) {
        super(request, writeBuffer);
    }

    /**
     * 输出Http消息头
     *
     * @throws IOException
     */
    final protected void writeHead() throws IOException {
        if (committed) {
            return;
        }

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        String headLine = request.getMethod() + " " + request.getUri() + " " + request.getProtocol();
        writeBuffer.write(getBytes(headLine));
        writeBuffer.write(Constant.CRLF);
        writeBuffer.write("1:1".getBytes());
        //转换Cookie
        convertCookieToHeader(request);

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

    private void convertCookieToHeader(AbstractRequest request) {
        List<Cookie> cookies = request.getCookies();
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        cookies.forEach(cookie -> {
            request.addHeader(HttpHeaderConstant.Names.SET_COOKIE, cookie.toString());
        });

    }
}
