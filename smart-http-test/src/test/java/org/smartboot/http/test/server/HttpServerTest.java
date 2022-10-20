/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerTest.java
 * Date: 2021-06-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.test.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.client.HttpGet;
import org.smartboot.http.client.HttpPost;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.test.BastTest;
import org.smartboot.socket.extension.plugins.StreamMonitorPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/6/4
 */
public class HttpServerTest extends BastTest {
    public static final String KEY_PARAMETERS = "parameters";
    public static final String KEY_METHOD = "method";
    public static final String KEY_URI = "uri";
    public static final String KEY_URL = "url";
    public static final String KEY_HEADERS = "headers";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerTest.class);
    private HttpBootstrap bootstrap;
    private RequestUnit requestUnit;

    @Before
    public void init() {
        bootstrap = new HttpBootstrap();
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(KEY_METHOD, request.getMethod());
                jsonObject.put(KEY_URI, request.getRequestURI());
                jsonObject.put(KEY_URL, request.getRequestURL());

                Map<String, String> parameterMap = new HashMap<>();
                request.getParameters().keySet().forEach(parameter -> parameterMap.put(parameter, request.getParameter(parameter)));
                jsonObject.put(KEY_PARAMETERS, parameterMap);

                Map<String, String> headerMap = new HashMap<>();
                request.getHeaderNames().forEach(headerName -> headerMap.put(headerName, request.getHeader(headerName)));
                jsonObject.put(KEY_HEADERS, headerMap);

                response.getOutputStream().write(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
            }
        }).setPort(SERVER_PORT);
        bootstrap.configuration().addPlugin(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM, StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));
        bootstrap.start();

        requestUnit = new RequestUnit();
        requestUnit.setUri("/hello");
        Map<String, String> headers = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            headers.put("header_" + i, UUID.randomUUID().toString());
        }
        headers.put("header_empty", "");
        requestUnit.setHeaders(headers);
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            params.put("params_" + i, UUID.randomUUID().toString());
        }
        requestUnit.setParameters(params);
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        StringBuilder uriStr = new StringBuilder(requestUnit.getUri()).append("?");
        requestUnit.getParameters().forEach((key, value) -> uriStr.append(key).append('=').append(value).append('&'));
        HttpGet httpGet = httpClient.get(uriStr.toString());
        requestUnit.getHeaders().forEach(httpGet::addHeader);

        JSONObject jsonObject = basicCheck(httpGet.send().get(), requestUnit);
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testGet1() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        requestUnit.getParameters().put("author", "三刀");
        HttpGet httpGet = httpClient.get(requestUnit.getUri());
        requestUnit.getParameters().forEach(httpGet::addQueryParam);
        requestUnit.getHeaders().forEach(httpGet::addHeader);

        JSONObject jsonObject = basicCheck(httpGet.send().get(), requestUnit);
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testGet2() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        org.smartboot.http.client.HttpResponse response = httpClient.get("/hello?").addQueryParam("author", "三刀").addQueryParam("abc", "123").send().get();

        JSONObject jsonObject = JSON.parseObject(response.body());
        JSONObject parameters = jsonObject.getJSONObject(KEY_PARAMETERS);
        Assert.assertEquals("123", parameters.get("abc"));
        Assert.assertEquals("三刀", parameters.get("author"));
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testGet3() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        org.smartboot.http.client.HttpResponse response = httpClient.get("/hello").addQueryParam("author", "三刀").addQueryParam("abc", "123").send().get();

        JSONObject jsonObject = JSON.parseObject(response.body());
        JSONObject parameters = jsonObject.getJSONObject(KEY_PARAMETERS);
        Assert.assertEquals("123", parameters.get("abc"));
        Assert.assertEquals("三刀", parameters.get("author"));
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testGet4() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        org.smartboot.http.client.HttpResponse response = httpClient.get("/hello#").addQueryParam("author", "三刀").addQueryParam("abc", "123").send().get();

        JSONObject jsonObject = JSON.parseObject(response.body());
        JSONObject parameters = jsonObject.getJSONObject(KEY_PARAMETERS);
        Assert.assertEquals("123", parameters.get("abc"));
        Assert.assertEquals("三刀", parameters.get("author"));
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testGet5() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        org.smartboot.http.client.HttpResponse response = httpClient.get("/hello#abca=sdf").addQueryParam("author", "三刀").addQueryParam("abc", "123").send().get();

        JSONObject jsonObject = JSON.parseObject(response.body());
        JSONObject parameters = jsonObject.getJSONObject(KEY_PARAMETERS);
        Assert.assertEquals("123", parameters.get("abc"));
        Assert.assertEquals("三刀", parameters.get("author"));
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testGet6() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        org.smartboot.http.client.HttpResponse response = httpClient.get("/hello?a=b#abca=sdf").addQueryParam("author", "三刀").addQueryParam("abc", "123").send().get();

        JSONObject jsonObject = JSON.parseObject(response.body());
        JSONObject parameters = jsonObject.getJSONObject(KEY_PARAMETERS);
        Assert.assertEquals("123", parameters.get("abc"));
        Assert.assertEquals("三刀", parameters.get("author"));
        Assert.assertEquals("b", parameters.get("a"));
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testGet7() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        org.smartboot.http.client.HttpResponse response = httpClient.get("/hello?#abca=sdf").addQueryParam("author", "三刀").addQueryParam("abc", "123").send().get();

        JSONObject jsonObject = JSON.parseObject(response.body());
        JSONObject parameters = jsonObject.getJSONObject(KEY_PARAMETERS);
        Assert.assertEquals("123", parameters.get("abc"));
        Assert.assertEquals("三刀", parameters.get("author"));
        Assert.assertEquals(HttpMethodEnum.GET.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testPost() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        HttpPost httpPost = httpClient.post(requestUnit.getUri());
        requestUnit.getHeaders().forEach(httpPost::addHeader);
        httpPost.sendForm(requestUnit.getParameters());

        JSONObject jsonObject = basicCheck(httpPost.send().get(), requestUnit);
        Assert.assertEquals(HttpMethodEnum.POST.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testPost1() throws ExecutionException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        HttpPost httpPost = httpClient.post(requestUnit.getUri());
        requestUnit.getHeaders().forEach(httpPost::addHeader);
        requestUnit.getParameters().put("author", "三刀");
        httpPost.sendForm(requestUnit.getParameters());

        JSONObject jsonObject = basicCheck(httpPost.send().get(), requestUnit);
        Assert.assertEquals(HttpMethodEnum.POST.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testPost2() throws ExecutionException, InterruptedException {
        bootstrap.configuration().readBufferSize(2 * 1024 * 1024);
        HttpClient httpClient = getHttpClient();
        HttpPost httpPost = httpClient.post(requestUnit.getUri());
        requestUnit.getHeaders().forEach(httpPost::addHeader);
        for (int i = 0; i < 10000; i++) {
            requestUnit.getParameters().put("author" + i, "三刀" + i);
        }
        httpPost.sendForm(requestUnit.getParameters());

        JSONObject jsonObject = basicCheck(httpPost.send().get(), requestUnit);
        Assert.assertEquals(HttpMethodEnum.POST.getMethod(), jsonObject.get(KEY_METHOD));
    }

    @Test
    public void testPost3() throws ExecutionException, InterruptedException {
        bootstrap.configuration().readBufferSize(2 * 1024 * 1024);
        HttpClient httpClient = getHttpClient();
        HttpPost httpPost = httpClient.post(requestUnit.getUri());
        requestUnit.getHeaders().forEach(httpPost::addHeader);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("author").append("=").append("三刀");
        for (int i = 0; i < 10000; i++) {
            stringBuilder.append("&").append("author").append(i).append("=").append("三刀").append(i);
        }
        httpPost.addHeader("longText", stringBuilder.toString());
        httpPost.sendForm(requestUnit.getParameters());

        JSONObject jsonObject = basicCheck(httpPost.send().get(), requestUnit);
        Assert.assertEquals(HttpMethodEnum.POST.getMethod(), jsonObject.get(KEY_METHOD));
    }

    private JSONObject basicCheck(org.smartboot.http.client.HttpResponse response, RequestUnit requestUnit) {
        JSONObject jsonObject = JSON.parseObject(response.body());
        LOGGER.info(JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat));
        Assert.assertEquals(requestUnit.getUri(), jsonObject.get(KEY_URI));

        JSONObject headerJson = jsonObject.getJSONObject(KEY_HEADERS);
        requestUnit.getHeaders().forEach((key, value) -> {
            Assert.assertEquals(value, headerJson.get(key));
        });

        JSONObject parameters = jsonObject.getJSONObject(KEY_PARAMETERS);
        requestUnit.getParameters().forEach((key, value) -> {
            Assert.assertEquals(value, parameters.get(key));
        });
        return jsonObject;
    }

    /**
     * 缓冲区溢出
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testHeaderValueOverflow() throws ExecutionException, InterruptedException {
        bootstrap.configuration().readBufferSize(16);
        HttpClient httpClient = getHttpClient();
        HttpPost httpPost = httpClient.post(requestUnit.getUri());
        requestUnit.getHeaders().forEach(httpPost::addHeader);
        httpPost.addHeader("overfLow", "1234567890abcdefghi");

        org.smartboot.http.client.HttpResponse response = httpPost.send().get();
        Assert.assertEquals(response.getStatus(), HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE.value());
    }

    /**
     * 缓冲区溢出
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testHeaderValueOverflow2() throws ExecutionException, InterruptedException {
        bootstrap.configuration().readBufferSize(16);
        HttpClient httpClient = getHttpClient();
        HttpPost httpPost = httpClient.post(requestUnit.getUri());
        httpPost.addHeader("1234567890abcdefghi", "1234567890abcdefghi");

        org.smartboot.http.client.HttpResponse response = httpPost.send().get();
        Assert.assertEquals(response.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @After
    public void destroy() {
        bootstrap.shutdown();
    }
}
