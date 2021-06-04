/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: BastTest.java
 * Date: 2021-05-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */
package org.smartboot.http.test;

import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.client.HttpResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/14
 */
public class BastTest {
    protected static final int SERVER_PORT = 8080;
    private static final Logger LOGGER = LoggerFactory.getLogger(BastTest.class);
    private static final String CONTENT_PATH = "/demo";

    protected HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient("127.0.0.1", SERVER_PORT);
        httpClient.connect();
        return httpClient;
    }


    protected void checkPath(String path, HttpClient smartClient, HttpClient tomcatClient) {
        Future<HttpResponse> smartFuture = smartClient.get(CONTENT_PATH + path).onSuccess(resp -> {
            LOGGER.info("smart-servlet response: {}", resp.body());
        }).send();
        Future<HttpResponse> tomcatFuture = tomcatClient.get(CONTENT_PATH + path).onSuccess(resp -> {
            LOGGER.info("tomcat response: {}", resp.body());
        }).send();
        try {
            checkResponse(smartFuture.get(), tomcatFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void checkResponse(HttpResponse smartResponse, HttpResponse tomcatResponse) {
        JSONObject smartJson = JSONObject.parseObject(smartResponse.body());
        JSONObject tomcatJson = JSONObject.parseObject(tomcatResponse.body());
        Assert.assertEquals("key 数量不一致", smartJson.size(), tomcatJson.size());
        for (String key : smartJson.keySet()) {
            Assert.assertEquals("key: " + key + " 匹配失败", smartJson.getString(key), tomcatJson.getString(key));
        }
    }
}
