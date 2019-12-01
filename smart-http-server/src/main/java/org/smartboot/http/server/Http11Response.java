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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
    private Map<String, HeaderValue> headers = null;
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
        if (headers != null) {
            headers.clear();
        }
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
        setHeader(name, value, true);
    }

    @Override
    public void addHeader(String name, String value) {
        setHeader(name, value, false);
    }

    /**
     * @param name    header name
     * @param value   header value
     * @param replace true:replace,false:append
     */
    private void setHeader(String name, String value, boolean replace) {
        char cc = name.charAt(0);
        if (cc == 'C' || cc == 'c') {
            if (checkSpecialHeader(name, value))
                return;
        }

        if (headers == null) {
            headers = new HashMap<>();
        }
        if (replace) {
            headers.put(name, new HeaderValue(null, value));
            return;
        }

        HeaderValue headerValue = headers.get(name);
        if (headerValue == null) {
            setHeader(name, value, true);
            return;
        }
        HeaderValue preHeaderValue = null;
        while (headerValue != null && !headerValue.getValue().equals(value)) {
            preHeaderValue = headerValue;
            headerValue = headerValue.getNextValue();
        }
        if (headerValue == null) {
            preHeaderValue.setNextValue(new HeaderValue(null, value));
        }
    }

    /**
     * 部分header需要特殊处理
     *
     * @param name
     * @param value
     * @return
     */
    private boolean checkSpecialHeader(String name, String value) {
        if (name.equalsIgnoreCase("Content-Type")) {
            setContentType(value);
            return true;
        }
        return false;
    }

    @Override
    public String getHeader(String name) {
        HeaderValue headerValue = null;
        if (headers != null) {
            headerValue = headers.get(name);
        }
        return headerValue == null ? null : headerValue.getValue();
    }

    Map<String, HeaderValue> getHeaders() {
        return headers;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        if (headers == null) {
            return Collections.emptyList();
        }
        Vector<String> result = new Vector<>();
        HeaderValue headerValue = headers.get(name);
        while (headerValue != null) {
            result.addElement(headerValue.getValue());
            headerValue = headerValue.getNextValue();
        }
        return result;
    }

    @Override
    public Collection<String> getHeaderNames() {
        if (headers == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(headers.size());
        for (String key : headers.keySet()) {
            result.add(key);
        }
        return result;
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
