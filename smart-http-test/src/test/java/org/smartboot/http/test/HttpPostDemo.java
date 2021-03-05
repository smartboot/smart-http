/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpPostDemo.java
 * Date: 2021-03-05
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.handle.HttpRouteHandle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huqiang
 * @since 2021/3/2 10:57
 */
public class HttpPostDemo {

    private HttpBootstrap httpBootstrap;

    @Before
    public void init() {
        httpBootstrap = new HttpBootstrap();
        HttpRouteHandle routeHandle = new HttpRouteHandle();
        routeHandle.route("/post_param", new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                for (String key : request.getParameters().keySet()) {
                    System.out.println(key + ": " + request.getParameter(key));
                }
            }
        });
        httpBootstrap.pipeline(routeHandle).setPort(8080).start();
    }

    @Test
    public void testPost() {
        HttpClient httpClient = new HttpClient("localhost", 8080);
        httpClient.connect();
        Map<String, String> param = new HashMap<>();
        param.put("name", "zhouyu");
        param.put("age", "18");
        httpClient.post("/post_param")
                .setContentType(HttpHeaderConstant.Values.X_WWW_FORM_URLENCODED)
                .onSuccess(response -> {
                    System.out.println(response.body());
                    httpClient.close();
                })
                .sendForm(param)
                .onFailure(throwable -> {
                    System.out.println("异常A: " + throwable.getMessage());
                    throwable.printStackTrace();
                });
    }

    @After
    public void destroy() {
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        httpBootstrap.shutdown();
    }
}
