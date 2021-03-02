/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.HttpHeaderConstant;
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

        // 添加content-type
        String contentType = request.getContentType();
        if (contentType == null) {
            contentType = HttpHeaderConstant.Values.DEFAULT_CONTENT_TYPE;
        }
        request.addHeader(HttpHeaderConstant.Names.CONTENT_TYPE, contentType);

        // 添加content-length
        byte[] body = getBody();
        request.addHeader(HttpHeaderConstant.Names.CONTENT_LENGTH, body == null?"0":String.valueOf(body.length));

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        String headLine = request.getMethod() + " " + request.getUri() + " " + request.getProtocol();
        writeBuffer.write(getBytes(headLine));
        //转换Cookie
        convertCookieToHeader(request);

        //输出Header部分
        if (request.getHeaders() != null) {
            for (Map.Entry<String, HeaderValue> entry : request.getHeaders().entrySet()) {
                HeaderValue headerValue = entry.getValue();
                while (headerValue != null) {
                    writeBuffer.write(getHeaderNameBytes(entry.getKey()));
                    writeBuffer.write(getBytes(headerValue.getValue()));
                    headerValue = headerValue.getNextValue();
                }
            }
        }
        writeBuffer.write(Constant.HEADER_END);

        if (body != null) {
            writeBuffer.write(body);
        }
        committed = true;
    }

    private byte[] getBody() {
        if (request.getParams() != null) {
            StringBuilder sb = new StringBuilder();
            Map<String, String> params = request.getParams();
            int idx = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (idx != 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                idx++;
            }
            return getBytes(sb.toString());
        }
        return null;
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
