package org.smartboot.http.restful.parameter;

import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;

public interface Invoker {
    Invoker HttpRequestHttpRequest = (request, response, context) -> request;

    Invoker HttpResponseHttpRequest = (request, response, context) -> response;

    Object invoker(HttpRequest request, HttpResponse response, InvokerContext context);

}
