/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: QueueUnit.java
 * Date: 2021-07-12
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;


import java.util.concurrent.CompletableFuture;

public class QueueUnit {
    private final CompletableFuture future;
    private final ResponseHandler responseHandler;

    public QueueUnit(CompletableFuture<? extends AbstractResponse> future, ResponseHandler responseHandler) {
        this.future = future;
        this.responseHandler = responseHandler;
    }

    public CompletableFuture<AbstractResponse> getFuture() {
        return future;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}