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
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/10/28
 */
public class AsyncHttpDemo {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.httpHandler(new HttpServerHandler() {

            @Override
            public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> future) throws IOException {
//                response.write((new Date() + " currentThread:" + Thread.currentThread()).getBytes());
//                response.getOutputStream().flush();
                executorService.execute(() -> {
                    try {
                        //sleep 3秒模拟阻塞
                        Thread.sleep(1000);
                        response.write(("<br/>" + new Date() + " currentThread:" + Thread.currentThread()).getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    future.complete(this);
                });

            }
        });
        bootstrap.configuration().debug(true);
        bootstrap.setPort(8080).start();
    }
}
