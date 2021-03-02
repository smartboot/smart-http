/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpPost.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.socket.transport.WriteBuffer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/4
 */
public class HttpPost extends HttpRest {

    public HttpPost(String uri, String host, WriteBuffer writeBuffer, Consumer<CompletableFuture<HttpResponse>> bindListener) {
        super(uri, host, writeBuffer, bindListener);
        request.setMethod(HttpMethodEnum.POST.getMethod());
    }

    @Override
    public HttpRest setMethod(String method) {
        throw new UnsupportedOperationException();
    }

    public HttpPost send(Map<String, String> params) {
        request.setParams(params);
        super.send();
        return this;
    }

    @Override
    public HttpPost onSuccess(Consumer<HttpResponse> consumer) {
        super.onSuccess(consumer);
        return this;
    }

    @Override
    public HttpPost onFailure(Consumer<Throwable> consumer) {
        super.onFailure(consumer);
        return this;
    }

    public HttpPost setContentType(String contentType){
        request.setContentType(contentType);
        return this;
    }

    @Override
    public HttpPost send() {
        super.send();
        return this;
    }
}
