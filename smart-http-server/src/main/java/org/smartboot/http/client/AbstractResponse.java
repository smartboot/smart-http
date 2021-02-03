/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractRequest.java
 * Date: 2021-02-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.Reset;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;

abstract class AbstractResponse implements HttpResponse, Reset, ResponseHook {

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
    public final String getRemoteAddr() {
        return response.getRemoteAddr();
    }

    @Override
    public final InetSocketAddress getRemoteAddress() {
        return response.getRemoteAddress();
    }

    @Override
    public final InetSocketAddress getLocalAddress() {
        return response.getLocalAddress();
    }

    @Override
    public final String getRemoteHost() {
        return response.getRemoteHost();
    }

    @Override
    public final Locale getLocale() {
        return response.getLocale();
    }

    @Override
    public final Enumeration<Locale> getLocales() {
        return response.getLocales();
    }

    @Override
    public final String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public final Response getResponse() {
        return response;
    }

    @Override
    public Cookie[] getCookies() {
        return response.getCookies();
    }

    @Override
    public <A> A getAttachment() {
        return response.getAttachment();
    }

    @Override
    public <A> void setAttachment(A attachment) {
        response.setAttachment(attachment);
    }
}
