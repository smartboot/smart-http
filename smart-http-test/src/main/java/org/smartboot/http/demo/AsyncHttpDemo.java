/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AsyncHttpDemo.java
 * Date: 2021-10-28
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.demo;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/10/28
 */
public class AsyncHttpDemo {
    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.httpHandler(new HttpServerHandler() {

            @Override
            public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> future) throws IOException {
                response.write("hello smart-http<br/>".getBytes());
//                future.complete(this);
                executorService.schedule(() -> {
                    future.complete(this);
                    System.out.println("finish");
                }, 3, TimeUnit.SECONDS);

            }
        });
        bootstrap.configuration().debug(false);
        bootstrap.setPort(8080).start();
    }
}
