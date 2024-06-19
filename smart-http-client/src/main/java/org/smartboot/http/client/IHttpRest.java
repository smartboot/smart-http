package org.smartboot.http.client;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface IHttpRest {
    Body<? extends IHttpRest> body();

    Future<HttpResponse> done();

    IHttpRest onSuccess(Consumer<HttpResponse> consumer);

    IHttpRest onFailure(Consumer<Throwable> consumer);

    Header<? extends IHttpRest> header();

    IHttpRest addQueryParam(String name, String value);

    IHttpRest onResponse(ResponseHandler responseHandler);
}
