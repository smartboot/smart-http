package org.smartboot.http.server.impl;

import org.smartboot.http.server.PushBuilder;

import java.util.HashSet;
import java.util.Set;

public class PushBuilderImpl implements PushBuilder {
    private final Http2RequestImpl pushRequest;

    public PushBuilderImpl(Http2Session session) {
        this.pushRequest = new Http2RequestImpl(session.getPushStreamId().addAndGet(2), session, true);
    }

    @Override
    public PushBuilder method(String method) {
        pushRequest.setMethod(method);
        pushRequest.getResponse().addHeader(":method", method);
        return this;
    }

    @Override
    public PushBuilder queryString(String queryString) {
        pushRequest.setQueryString(queryString);
        return this;
    }

    @Override
    public PushBuilder setHeader(String name, String value) {
        pushRequest.getResponse().setHeader(name, value);
        return this;
    }

    @Override
    public PushBuilder addHeader(String name, String value) {
        pushRequest.getResponse().addHeader(name, value);
        return this;
    }

    @Override
    public PushBuilder removeHeader(String name) {
        pushRequest.getResponse().setHeader(name, null);
        return null;
    }

    @Override
    public PushBuilder path(String path) {
        pushRequest.setUri(path);
        pushRequest.setRequestURI(path);
        return this;
    }

    @Override
    public void push() {
        pushRequest.getSession().getRequest().getConfiguration().getHttp2ServerHandler().handleHttpRequest(pushRequest);
    }

    @Override
    public String getMethod() {
        return pushRequest.getMethod();
    }

    @Override
    public String getQueryString() {
        return pushRequest.getQueryString();
    }

    @Override
    public String getSessionId() {
        return "";
    }

    @Override
    public Set<String> getHeaderNames() {
        return new HashSet<>(pushRequest.getHeaderNames());
    }

    @Override
    public String getHeader(String name) {

        return pushRequest.getResponse().getHeader(name);
    }

    @Override
    public String getPath() {
        return pushRequest.getRequestURI();
    }
}
