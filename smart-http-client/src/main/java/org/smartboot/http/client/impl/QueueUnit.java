/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: QueueUnit.java
 * Date: 2021-07-12
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.HttpResponse;
import org.smartboot.http.client.ResponseHandler;

import java.util.concurrent.CompletableFuture;

public class QueueUnit {
    private final CompletableFuture<HttpResponse> future;
    private final ResponseHandler responseHandler;

    public QueueUnit(CompletableFuture<HttpResponse> future, ResponseHandler responseHandler) {
        this.future = future;
        this.responseHandler = responseHandler;
    }

    public CompletableFuture<HttpResponse> getFuture() {
        return future;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}