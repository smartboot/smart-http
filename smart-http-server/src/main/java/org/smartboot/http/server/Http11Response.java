/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: DefaultHttpResponse.java
 * Date: 2018-02-17
 * Author: sandao
 */

package org.smartboot.http.server;

import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
class Http11Response implements HttpResponse {
    /**
     * 输入流
     */
    private final HttpOutputStream outputStream;

    /**
     * 响应消息头
     */
    private Map<String, String> headers = new HashMap<>();
    /**
     * http响应码
     */
    private HttpStatus httpStatus;
    /**
     * 响应正文长度
     */
    private int contentLength = -1;

    /**
     * 正文编码方式
     */
    private String contentType;


    public Http11Response(Http11Request request, OutputStream outputStream) {
        this.outputStream = new HttpOutputStream(request, this, outputStream);
    }


    public void reset() {
        headers.clear();
        httpStatus = null;
        contentType = null;
        contentLength = -1;
        this.outputStream.reset();
    }


    public OutputStream getOutputStream() {
        return outputStream;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void write(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 输出流是否已关闭
     *
     * @return
     */
    public boolean isClosed() {
        return outputStream.isClosed();
    }
}
