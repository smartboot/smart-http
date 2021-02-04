/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpPost.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/4
 */
public class HttpPost extends HttpRest {

    public HttpPost(String host, String uri, WriteBuffer writeBuffer, Consumer<CompletableFuture<HttpResponse>> bindListener) {
        super(host, uri, writeBuffer, bindListener);
        request.setMethod(HttpMethodEnum.POST.getMethod());
    }

    public HttpPost sendBuffer(String body) {
        try {
            request.getOutputStream().write(body.getBytes());
            super.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public HttpPost send() {

        super.send();
        return this;
    }
}
