/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpResponseImpl.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HttpProtocolEnum;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
class HttpResponseImpl extends AbstractResponse {
    private final HttpRequestImpl request;

    public HttpResponseImpl(HttpRequestImpl request) {
        init(request.request.getAioSession(), new HttpOutputStream(request, this));
        this.request = request;
    }

    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        if (outputStream.isCommitted()) {
            throw new IllegalStateException();
        }
        if (Objects.equals(request.getProtocol(), HttpProtocolEnum.HTTP_10.getProtocol())) {
            throw new IllegalStateException("HTTP/1.0 request");
        } else if (Objects.equals(request.getProtocol(), HttpProtocolEnum.HTTP_11.getProtocol()) && !outputStream.isChunkedSupport()) {
            throw new IllegalStateException("unSupport trailer");
        }
        outputStream.setTrailerFields(supplier);
    }
}
