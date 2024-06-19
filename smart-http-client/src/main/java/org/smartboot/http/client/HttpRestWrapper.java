package org.smartboot.http.client;

import java.util.concurrent.Future;
import java.util.function.Consumer;

class HttpRestWrapper implements IHttpRest {
    protected final HttpRest rest;

    public HttpRestWrapper(HttpRest rest) {
        this.rest = rest;
    }

    @Override
    public Body<? extends IHttpRest> body() {
        return rest.body();
    }

    @Override
    public Future<HttpResponse> done() {
        return rest.done();
    }

    @Override
    public IHttpRest onSuccess(Consumer<HttpResponse> consumer) {
        return rest.onSuccess(consumer);
    }

    @Override
    public IHttpRest onFailure(Consumer<Throwable> consumer) {
        return rest.onFailure(consumer);
    }

    @Override
    public Header<? extends IHttpRest> header() {
        return rest.header();
    }

    @Override
    public IHttpRest addQueryParam(String name, String value) {
        return rest.addQueryParam(name, value);
    }

    @Override
    public IHttpRest onResponse(ResponseHandler responseHandler) {
        return rest.onResponse(responseHandler);
    }
}
