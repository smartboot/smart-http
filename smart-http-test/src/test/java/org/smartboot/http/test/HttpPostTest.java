/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpPostDemo.java
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
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.handle.HttpRouteHandle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author huqiang
 * @since 2021/3/2 10:57
 */
public class HttpPostTest {

    private HttpBootstrap httpBootstrap;

    @Before
    public void init() {
        httpBootstrap = new HttpBootstrap();
        HttpRouteHandle routeHandle = new HttpRouteHandle();
        routeHandle.route("/post_param", new HttpServerHandle() {
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
        Map<String, String> param = new HashMap<>();
        param.put("name", "zhouyu");
        param.put("age", "18");
        httpClient.post("/post_param")
                .setContentType(HeaderValueEnum.X_WWW_FORM_URLENCODED.getName())
                .onSuccess(response -> {
                    System.out.println(response.body());
                    JSONObject jsonObject = JSONObject.parseObject(response.body());
                    boolean suc = false;
                    for (String key : param.keySet()) {
                        suc = StringUtils.equals(param.get(key), jsonObject.getString(key));
                        if (!suc) {
                            break;
                        }
                    }
                    httpClient.close();
                    future.complete(suc);
                })
                .onFailure(throwable -> {
                    System.out.println("异常A: " + throwable.getMessage());
                    throwable.printStackTrace();
                    Assert.fail();
                    future.complete(false);
                }).sendForm(param);
        Assert.assertTrue(future.get());
    }

    @After
    public void destroy() {
        httpBootstrap.shutdown();
    }
}
