/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.BufferOutputStream;
import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpServerConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
abstract class AbstractOutputStream extends BufferOutputStream {


    protected final AbstractResponse response;
    protected final HttpRequest request;
    protected final HttpServerConfiguration configuration;

    public AbstractOutputStream(HttpRequest httpRequest, AbstractResponse response, Request request) {
        super(request.getAioSession());
        this.response = response;
        this.request = httpRequest;
        this.configuration = request.getConfiguration();
    }



    /**
     * 输出Http消息头
     */
    protected final void writeHead() throws IOException {
        if (committed) {
            return;
        }
        //转换Cookie
        convertCookieToHeader();

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        writeBuffer.write(getHeadPart());

        //输出Header部分
        writeHeader();

        committed = true;
    }

    private void convertCookieToHeader() {
        List<Cookie> cookies = response.getCookies();
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        cookies.forEach(cookie -> response.addHeader(HeaderNameEnum.SET_COOKIE.getName(), cookie.toString()));
    }

    protected abstract byte[] getHeadPart();

    protected boolean hasHeader() {
        return response.getHeaders() != null && response.getHeaders().size() > 0;
    }

    private void writeHeader() throws IOException {
        if (response.getHeaders() != null) {
            for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
                HeaderValue headerValue = entry.getValue();
                while (headerValue != null) {
                    writeBuffer.write(getHeaderNameBytes(entry.getKey()));
                    writeBuffer.write(getBytes(headerValue.getValue()));
                    writeBuffer.write(Constant.CRLF_BYTES);
                    headerValue = headerValue.getNextValue();
                }
            }
            if (response.getHeaders().size() > 0) {
                writeBuffer.write(Constant.CRLF_BYTES);
            }
        }
    }

    @Override
    public void close() throws IOException {
        //识别是否采用 chunked 输出
        if (!committed) {
            chunked = supportChunked(request, response);
        }
        super.close();
    }

    @Override
    protected final void check() {
        if (HttpMethodEnum.HEAD.getMethod().equals(request.getMethod())) {
            throw new UnsupportedOperationException(request.getMethod() + " can not write http body");
        }
        //识别是否采用 chunked 输出
        if (!committed) {
            chunked = supportChunked(request, response);
        }
    }

    /**
     * 是否支持chunked输出
     *
     * @return
     */
    private boolean supportChunked(HttpRequest request, AbstractResponse response) {
        return (request.getMethod() == HttpMethodEnum.GET.getMethod()
                || request.getMethod() == HttpMethodEnum.POST.getMethod())
                && response.getHttpStatus() != HttpStatus.CONTINUE.value()
                && response.getContentLength() < 0
                && HttpProtocolEnum.HTTP_10.getProtocol() != request.getProtocol();
    }
}
