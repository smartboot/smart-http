/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpV2Entity.java
 * Date: 2018-01-23
 * Author: sandao
 */

package org.smartboot.http.common;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Map;

/**
 * Http消息体，兼容请求与响应
 *
 * @author 三刀 2018/06/02
 */
public abstract class HttpEntity {

    protected HttpHeader header;

    public HttpEntity(HttpHeader header) {
        this.header = header;
    }


    public HttpHeader getHeader() {
        return header;
    }

    public void setHeader(String name, String value) {
        header.setHeader(name, value);
    }

    public String getHeader(String name) {
        return header.getHeader(name);
    }

    public Map<String, String> getHeaders() {
        return header.headerMap;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
