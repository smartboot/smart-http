/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: QueueUnit.java
 * Date: 2021-07-12
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.HttpResponse;
import org.smartboot.http.client.HttpRest;

import java.util.concurrent.CompletableFuture;

public class QueueUnit {
    private HttpRest httpRest;
    private CompletableFuture<HttpResponse> future;

    public QueueUnit(HttpRest httpRest, CompletableFuture<HttpResponse> future) {
        this.httpRest = httpRest;
        this.future = future;
    }

    public HttpRest getHttpRest() {
        return httpRest;
    }

    public CompletableFuture<HttpResponse> getFuture() {
        return future;
    }
}