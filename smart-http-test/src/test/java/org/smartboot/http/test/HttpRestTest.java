/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRestDemo.java
 * Date: 2021-03-05
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.test;

import com.alibaba.fastjson.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.handle.HttpRouteHandle;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/7
 */
public class HttpRestTest {

    private HttpBootstrap httpBootstrap;


    @Before
    public void init() {
        httpBootstrap = new HttpBootstrap();
        HttpRouteHandle routeHandle = new HttpRouteHandle();
        routeHandle.route("/post", new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                JSONObject jsonObject = new JSONObject();
                for (String key : request.getParameters().keySet()) {
                    jsonObject.put(key, request.getParameter(key));
                }
                response.write(jsonObject.toString().getBytes());
            }
        });
        httpBootstrap.pipeline(routeHandle).setPort(8080).start();
    }

    @Test
    public void testPost() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        HttpClient httpClient = new HttpClient("localhost", 8080);
        httpClient.connect();
        httpClient.rest("/post")
                .setMethod("post")
                .onSuccess(response -> {
                    System.out.println(response.body());
                    httpClient.close();
                    future.complete(true);
                })
                .onFailure(throwable -> {
                    throwable.printStackTrace();
                    httpClient.close();
                    future.complete(true);
                })
                .send();
        Assert.assertTrue(future.get());
    }

    @After
    public void destroy() {
        httpBootstrap.shutdown();
    }

}
