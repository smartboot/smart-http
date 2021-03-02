/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractRequest.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.HttpRequest;
import org.smartboot.http.common.BufferOutputStream;
import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;

import java.io.IOException;
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
class AbstractRequest implements HttpRequest {
    private String uri;
    private String protocol;
    /**
     * 请求类型
     */
    private String method;
    /**
     * 输入流
     */
    private AbstractOutputStream outputStream;

    /**
     * 响应消息头
     */
    private Map<String, HeaderValue> headers = null;

    /**
     * 请求正文
     */
    private Map<String, String> params;

    /**
     * 响应正文长度
     */
    private int contentLength = -1;

    /**
     * 正文编码方式
     */
    private String contentType;

    private List<Cookie> cookies;

    protected void init(AbstractOutputStream outputStream) {
        this.outputStream = outputStream;
    }


    public final BufferOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public final void setHeader(String name, String value) {
        setHeader(name, value, true);
    }

    @Override
    public final void addHeader(String name, String value) {
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
            checkSpecialHeader(name, value);
        }

        if (headers == null) {
            headers = new HashMap<>();
        }
        if (replace) {
            if (value == null) {
                headers.remove(name);
            } else {
                headers.put(name, new HeaderValue(null, value));
            }
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
     */
    private void checkSpecialHeader(String name, String value) {
        if (name.equalsIgnoreCase("Content-Type")) {
            setContentType(value);
        }
    }

    @Override
    public final String getHeader(String name) {
        HeaderValue headerValue = null;
        if (headers != null) {
            headerValue = headers.get(name);
        }
        return headerValue == null ? null : headerValue.getValue();
    }

    final Map<String, HeaderValue> getHeaders() {
        return headers;
    }

    @Override
    public final Collection<String> getHeaders(String name) {
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
    public final Collection<String> getHeaderNames() {
        if (headers == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(headers.keySet());
    }


    public final void write(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new ArrayList<>();
        }
        cookies.add(cookie);
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public final String getContentType() {
        return contentType;
    }

    public final void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
