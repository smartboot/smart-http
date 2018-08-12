/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: Http11Request.java
 * Date: 2018-02-16
 * Author: sandao
 */

package org.smartboot.http.server.http11;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.smartboot.http.common.HttpEntityV2;
import org.smartboot.http.common.enums.MethodEnum;
import org.smartboot.http.common.utils.EmptyInputStream;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 三刀 on 2017/6/20.
 */
public class Http11Request extends HttpEntityV2 {


    private InputStream inputStream = null;
    private int contentLength = -1;
    private String contentType;
    private String httpVersion;

    private String requestURI;
    /**
     * http://localhost?aa=aa  ?后面部分
     */
    private String queryString;

    //HTTP\HTTPS...
    private Map<String, String> paramMap = new HashMap<String, String>();


    public InputStream getInputStream() {
        return inputStream == null ? new EmptyInputStream() : inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getHeader(String key) {
        return null;
    }


    public MethodEnum getMethodRange() {
        byte[] b = getBytes(methodRange);
        return MethodEnum.getByMethod(b, 0, b.length);
    }

    public String getOriginalUri() {
        return get(uriRange);
    }


    public String getHttpVersion() {
        return httpVersion == null ? httpVersion = get(protocolRange) : httpVersion;
    }


    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


    public void setParam(String key, String val) {
        this.paramMap.put(key, val);
    }

    public String getProtocol() {
        return get(protocolRange);
    }


}