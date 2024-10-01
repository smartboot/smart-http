/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRestTest.java
 * Date: 2021-06-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.test.client;

import com.alibaba.fastjson.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.handler.HttpRouteHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/7
 */
public class HttpRestTest {

    private HttpBootstrap httpBootstrap;


    @Before
    public void init() {
        httpBootstrap = new HttpBootstrap();
        HttpRouteHandler routeHandler = new HttpRouteHandler();
        routeHandler.route("/post", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                response.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.keepalive.getName());
                JSONObject jsonObject = new JSONObject();
                for (String key : request.getParameters().keySet()) {
                    jsonObject.put(key, request.getParameter(key));
                }
                response.write(jsonObject.toString().getBytes());
            }
        });
        httpBootstrap.httpHandler(routeHandler).setPort(8080).start();
    }

    @Test
    public void testPost() throws ExecutionException, InterruptedException {
        HttpClient httpClient = new HttpClient("localhost", 8080);
        Future<org.smartboot.http.client.HttpResponse> future = httpClient.rest("/post")
                .setMethod("post")
                .onSuccess(response -> {
                    System.out.println(response.body());
                    httpClient.close();
                })
                .onFailure(throwable -> {
                    throwable.printStackTrace();
                    httpClient.close();
                })
                .done();
        System.out.println(future.get().body());
    }

    @Test
    public void testKeepalive() throws InterruptedException {
        HttpClient httpClient = new HttpClient("localhost", 8080);
        httpClient.configuration().debug(true);
        Map<String, String> form = new HashMap<>();
        int count = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            form.put("name" + i, "value" + i);
            httpClient.post("/post").header().keepalive(true).done()
                    .body().formUrlencoded(form)
                    .onSuccess(httpResponse -> {
                        countDownLatch.countDown();
                        System.out.println(httpResponse.body());
                    })
                    .onFailure(throwable -> {
                        countDownLatch.countDown();
                        throwable.printStackTrace();
                    }).done();
        }
        countDownLatch.await();
        System.out.println("finish...");
    }

    @After
    public void destroy() {
        httpBootstrap.shutdown();
    }

}
