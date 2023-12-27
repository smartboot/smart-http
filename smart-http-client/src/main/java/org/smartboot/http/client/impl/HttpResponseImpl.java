package org.smartboot.http.client.impl;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.client.HttpResponse;
import org.smartboot.socket.transport.AioSession;

public class HttpResponseImpl extends AbstractResponse implements HttpResponse {
    /**
     * body内容
     */
    private String body;

    public HttpResponseImpl(AioSession session) {
        super(session);
    }

    public String body() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public void reset() {

    }
}
