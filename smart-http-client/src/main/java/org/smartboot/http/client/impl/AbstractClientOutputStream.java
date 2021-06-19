/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractOutputStream.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.common.AbstractOutputStream;
import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
abstract class AbstractClientOutputStream extends AbstractOutputStream {

    protected final AbstractRequest request;

    public AbstractClientOutputStream(AbstractRequest request, WriteBuffer writeBuffer) {
        super(writeBuffer);
        this.request = request;
    }


    /**
     * 输出Http消息头
     *
     * @throws IOException
     */
    protected final void writeHead() throws IOException {
        if (committed) {
            return;
        }
        this.chunked = false;
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
        committed = true;
    }

    @Override
    protected final void check() {

    }

    private void convertCookieToHeader(AbstractRequest request) {
        List<Cookie> cookies = request.getCookies();
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        cookies.forEach(cookie -> request.addHeader(HeaderNameEnum.SET_COOKIE.getName(), cookie.toString()));

    }
}
