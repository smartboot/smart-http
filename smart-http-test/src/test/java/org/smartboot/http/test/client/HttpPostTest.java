/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpPostTest.java
 * Date: 2021-06-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.test.client;

import com.alibaba.fastjson.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.handler.HttpRouteHandler;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.util.AttachKey;
import org.smartboot.socket.util.Attachment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author huqiang
 * @since 2021/3/2 10:57
 */
public class HttpPostTest {

    private HttpBootstrap httpBootstrap;

    @Before
    public void init() {
        httpBootstrap = new HttpBootstrap();
        HttpRouteHandler routeHandle = new HttpRouteHandler();
        routeHandle.route("/post_param", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                JSONObject jsonObject = new JSONObject();
                for (String key : request.getParameters().keySet()) {
                    jsonObject.put(key, request.getParameter(key));
                }
                response.write(jsonObject.toString().getBytes());
            }
        });
        routeHandle.route("/json", new HttpServerHandler() {
            private AttachKey<ByteBuffer> bodyKey = AttachKey.valueOf("bodyKey");

            @Override
            public boolean onBodyStream(ByteBuffer buffer, Request request) {
                Attachment attachment = request.getAttachment();
                ByteBuffer bodyBuffer = null;
                if (attachment != null) {
                    bodyBuffer = attachment.get(bodyKey);
                }
                if (bodyBuffer == null) {
                    bodyBuffer = ByteBuffer.allocate(request.getContentLength());
                    if (attachment == null) {
                        attachment = new Attachment();
                        request.setAttachment(attachment);
                    }
                    attachment.put(bodyKey, bodyBuffer);
                }
                if (buffer.remaining() <= bodyBuffer.remaining()) {
                    bodyBuffer.put(buffer);
                } else {
                    int limit = buffer.limit();
                    buffer.limit(buffer.position() + bodyBuffer.remaining());
                    bodyBuffer.put(buffer);
                    buffer.limit(limit);
                }
                return !bodyBuffer.hasRemaining();
            }

            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                ByteBuffer buffer = request.getAttachment().get(bodyKey);
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                System.out.println(new String(bytes));
            }
        });
        routeHandle.route("/header", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                JSONObject jsonObject = new JSONObject();
                for (String header : request.getHeaderNames()) {
                    jsonObject.put(header, request.getHeader(header));
                }
                response.write(jsonObject.toJSONString().getBytes());
            }
        });

        routeHandle.route("/other/abc", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                System.out.println("--");
                InputStream inputStream = request.getInputStream();
                byte[] bytes = new byte[1024];
                int size;
                while ((size = inputStream.read(bytes)) != -1) {
                    response.getOutputStream().write(bytes, 0, size);
                }
            }
        });

        routeHandle.route("/chunk", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws Throwable {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InputStream inputStream = request.getInputStream();
                int b;
                while ((b = inputStream.read()) != -1) {
                    byteArrayOutputStream.write(b);
                    response.write(new byte[]{(byte) b});
                }

                System.out.println(byteArrayOutputStream.toString());
            }
        });

        httpBootstrap.httpHandler(routeHandle).setPort(8080).start();
    }

    @Test
    public void testChunkedRequest() throws ExecutionException, InterruptedException {
        HttpClient client = new HttpClient("127.0.0.1", 8080);
        String body = "test a body string";
        client.configuration().debug(true);
        Future<org.smartboot.http.client.HttpResponse> future = client.post("/chunk")
                .header().keepalive(true).done()
                .body()
                .write(body.getBytes()).write(body.getBytes())
                .done()
                .onSuccess(response -> {
                    System.out.println(response.body());
                })
                .onFailure(t -> {
                    System.out.println(t.getMessage());
                }).done();
//        JSONObject jsonObject = JSONObject.parseObject(future.get().body());
        Assert.assertEquals(future.get().body(), body + body);
    }

    @Test
    public void testCheckHeader() throws InterruptedException, ExecutionException {
        HttpClient client = new HttpClient("127.0.0.1", 8080);
        String body = "test a body string";
        client.configuration().debug(true);
        Future<org.smartboot.http.client.HttpResponse> future = client.post("/header")
                .header().keepalive(true).setContentLength(body.getBytes().length).done()
                .body()
                .write(body.getBytes())
                .done()
                .onSuccess(response -> {
                    System.out.println(response.body());
                })
                .onFailure(t -> {
                    System.out.println(t.getMessage());
                }).done();
        JSONObject jsonObject = JSONObject.parseObject(future.get().body());
        Assert.assertNull(jsonObject.getString(HeaderNameEnum.TRANSFER_ENCODING.getName()));
        Assert.assertEquals(jsonObject.getString(HeaderNameEnum.CONTENT_LENGTH.getName()), String.valueOf(body.getBytes().length));
    }

    @Test
    public void testChunked() throws InterruptedException, ExecutionException {
        HttpClient client = new HttpClient("127.0.0.1", 8080);
        String body = "test a body string";
        String body2 = "test a body2 string";
        client.configuration().debug(true);
        Future<org.smartboot.http.client.HttpResponse> future1 = client.post("/other/abc")
                .header().keepalive(true).setContentLength(body.getBytes().length).done()
                .body()
                .write(body.getBytes())
                .done()
                .onSuccess(response -> {
                    System.out.println(response.body());
                    System.out.println("1111");
                })
                .onFailure(t -> {
                    System.out.println(t.getMessage());
                }).done();

        Future<org.smartboot.http.client.HttpResponse> future2 = client.post("/other/abc")
                .header().keepalive(true).setContentLength(body2.getBytes().length).done()
                .body()
                .write(body2.getBytes())
                .done()
                .onSuccess(response -> {
                    System.out.println(response.body());
                    System.out.println("222");
                })
                .onFailure(t -> {
                    System.out.println(t.getMessage());
                }).done();
        Assert.assertEquals(body, future1.get().body());
        Assert.assertEquals(body2, future2.get().body());
    }

    @Test
    public void testPost() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        HttpClient httpClient = new HttpClient("localhost", 8080);
        httpClient.configuration().debug(true);
        Map<String, String> param = new HashMap<>();
        param.put("name", "zhouyu");
        param.put("age", "18");
        httpClient.post("/post_param").header().setContentType(HeaderValueEnum.X_WWW_FORM_URLENCODED.getName()).done().onSuccess(response -> {
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
        }).onFailure(throwable -> {
            System.out.println("异常A: " + throwable.getMessage());
            throwable.printStackTrace();
            Assert.fail();
            future.complete(false);
        }).body().formUrlencoded(param);
        Assert.assertTrue(future.get());
    }

    @Test
    public void testJson() throws InterruptedException {
        HttpClient httpClient = new HttpClient("localhost", 8080);
        httpClient.configuration().debug(true);
        byte[] jsonBytes = "{\"a\":1,\"b\":\"123\"}".getBytes(StandardCharsets.UTF_8);
        httpClient.post("/json").header().setContentLength(jsonBytes.length).setContentType("application/json").done().body().write(jsonBytes).flush().done();
        Thread.sleep(100);
    }

    @After
    public void destroy() {
        httpBootstrap.shutdown();
    }
}
