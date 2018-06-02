/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpV2Entity.java
 * Date: 2018-01-23
 * Author: sandao
 */

package org.smartboot.http;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Http消息体，兼容请求与响应
 * Created by 三刀 on 2017/6/20.
 */
public abstract class HttpEntity {

    protected HttpHeader header;

    public HttpEntity(HttpHeader header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public HttpHeader getHeader() {
        return header;
    }
}
