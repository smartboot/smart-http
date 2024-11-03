package org.smartboot.http.server.impl;

import org.smartboot.http.common.codec.h2.codec.ContinuationFrame;
import org.smartboot.http.common.codec.h2.codec.Http2Frame;
import org.smartboot.http.common.codec.h2.codec.PushPromiseFrame;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.utils.HttpUtils;
import org.smartboot.http.server.PushBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PushBuilderImpl implements PushBuilder {
    public static final List<String> IGNORE_HEADERS = Arrays.asList("if-match", "if-none-match", "if-modified-since", "if-unmodified-since", "if-range", "range", "proxy-authorization", "from", "user-agent", "range", "expect", "max-forwards", "proxy-authenticate", "proxy-authorization", "age", "cache-control", "clear-site-data");
    private final Http2RequestImpl pushRequest;
    private final int streamId;

    public PushBuilderImpl(int streamId, Http2ResponseImpl response, Http2Session session) {
        this.streamId = streamId;
        this.pushRequest = new Http2RequestImpl(session.getPushStreamId().addAndGet(2), session, true);
        response.getCookies().forEach(cookie -> pushRequest.addHeader("Cookie", cookie.toString()));

        method(HttpMethodEnum.GET.getMethod());
    }

    @Override
    public PushBuilder method(String method) {
        pushRequest.setMethod(method);
        return this;
    }

    @Override
    public PushBuilder queryString(String queryString) {
        pushRequest.setQueryString(queryString);
        return this;
    }

    @Override
    public PushBuilder setHeader(String name, String value) {
        pushRequest.setHeader(name, value);
        return this;
    }

    @Override
    public PushBuilder addHeader(String name, String value) {
        pushRequest.addHeader(name, value);
        return this;
    }

    @Override
    public PushBuilder removeHeader(String name) {
        pushRequest.setHeader(name, null);
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
        try {
            pushRequest.setHeader(":method", pushRequest.getMethod());
            pushRequest.setHeader(":scheme", pushRequest.getScheme());
            pushRequest.setHeader(":path", pushRequest.getRequestURI());
            pushRequest.setHeader(":authority", pushRequest.getSession().getRequest().getHost());
            List<ByteBuffer> buffers = HttpUtils.HPackEncoder(pushRequest.getSession().getHpackEncoder(), pushRequest.getHeaders());
            PushPromiseFrame frame = new PushPromiseFrame(streamId, buffers.size() > 1 ? 0 : Http2Frame.FLAG_END_HEADERS, 0);
            frame.setPromisedStream(pushRequest.getStreamId());
            if (!buffers.isEmpty()) {
                frame.setFragment(buffers.get(0));
            }
            frame.writeTo(pushRequest.getSession().getRequest().aioSession.writeBuffer());
            for (int i = 1; i < buffers.size() - 1; i++) {
                ContinuationFrame continuationFrame = new ContinuationFrame(streamId, 0, 0);
                continuationFrame.setFragment(buffers.get(i));
                continuationFrame.writeTo(pushRequest.getSession().getRequest().aioSession.writeBuffer());
            }
            if (buffers.size() > 1) {
                ContinuationFrame continuationFrame = new ContinuationFrame(streamId, Http2Frame.FLAG_END_HEADERS, 0);
                continuationFrame.setFragment(buffers.get(buffers.size() - 1));
                continuationFrame.writeTo(pushRequest.getSession().getRequest().aioSession.writeBuffer());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        return pushRequest.getHeader(name);
    }

    @Override
    public String getPath() {
        return pushRequest.getRequestURI();
    }
}
