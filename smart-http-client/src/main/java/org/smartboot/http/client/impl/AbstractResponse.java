/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractResponse.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.HttpResponse;

import java.util.Collection;

abstract class AbstractResponse implements HttpResponse {

    protected Response response;

    protected void init(Response response) {
        this.response = response;
    }


    @Override
    public final String getHeader(String headName) {
        return response.getHeader(headName);
    }

    @Override
    public final Collection<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    @Override
    public final Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }

    @Override
    public final String getProtocol() {
        return response.getProtocol();
    }

    @Override
    public final String getContentType() {
        return response.getContentType();
    }

    @Override
    public final int getContentLength() {
        return response.getContentLength();
    }

    @Override
    public final String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public final String body() {
        return response.body();
    }
}
