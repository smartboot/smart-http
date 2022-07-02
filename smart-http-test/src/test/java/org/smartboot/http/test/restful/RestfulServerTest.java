/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerTest.java
 * Date: 2021-06-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.test.restful;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.restful.RestfulBootstrap;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.test.BastTest;
import org.smartboot.http.test.server.RequestUnit;
import org.smartboot.socket.extension.plugins.StreamMonitorPlugin;

import java.util.concurrent.ExecutionException;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/6/4
 */
public class RestfulServerTest extends BastTest {
    public static final String KEY_PARAMETERS = "parameters";
    public static final String KEY_METHOD = "method";
    public static final String KEY_URI = "uri";
    public static final String KEY_URL = "url";
    public static final String KEY_HEADERS = "headers";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulServerTest.class);
    private HttpBootstrap bootstrap;
    private RequestUnit requestUnit;


    @Before
    public void init() {
        bootstrap = RestfulBootstrap.controller(Demo1Controller.class, Demo2Controller.class);
        bootstrap.setPort(SERVER_PORT);
        bootstrap.configuration().addPlugin(new StreamMonitorPlugin<>((asynchronousSocketChannel, bytes) -> System.out.println(new String(bytes)), (asynchronousSocketChannel, bytes) -> System.out.println(new String(bytes))));
        bootstrap.start();
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();

        Assert.assertEquals(httpClient.get("/").send().get().body(), "hello");
    }

    @Test
    public void testGet2() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        Assert.assertEquals(httpClient.get("/demo2").send().get().body(), "hello world");
    }


    @After
    public void destroy() {
        bootstrap.shutdown();
    }
}
