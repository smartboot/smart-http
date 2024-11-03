package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.server.PushBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PushBuilderImpl implements PushBuilder {
    private final Http2RequestImpl pushRequest;

    public PushBuilderImpl(int streamId, Http2ResponseImpl response, Http2Session session) {
        this.pushRequest = new Http2RequestImpl(streamId, session, true);
        pushRequest.getResponse().setHeader(":authority", session.getRequest().getHost());
        pushRequest.getResponse().setHeader(":scheme", session.getRequest().getScheme());
        response.getCookies().forEach(cookie -> pushRequest.getResponse().addHeader("Cookie", cookie.toString()));
        method(HttpMethodEnum.GET.getMethod());
    }

    @Override
    public PushBuilder method(String method) {
        pushRequest.setMethod(method);
        pushRequest.getResponse().setHeader(":method", method);
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
        pushRequest.getResponse().setHeader(":path", path);
        return this;
    }

    @Override
    public void push() {
//        Executors.callable(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                pushRequest.getSession().getRequest().getConfiguration().getHttp2ServerHandler().handleHttpRequest(pushRequest);
//            }
//        });
        pushRequest.getSession().getRequest().getConfiguration().getHttp2ServerHandler().handleHttpRequest(pushRequest);
//        pushRequest.getSession().getRequest().aioSession.writeBuffer().flush();
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
