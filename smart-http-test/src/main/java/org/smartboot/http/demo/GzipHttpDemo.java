/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: GzipHttpDemo.java
 * Date: 2021-10-24
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.demo;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.handler.HttpRouteHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/10/24
 */
public class GzipHttpDemo {
    public static void main(String[] args) {
        String text = "Hello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello World";
        HttpRouteHandler routeHandle = new HttpRouteHandler();
        routeHandle.route("/a", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                byte[] data = text.getBytes();
                response.setContentLength(data.length);
//                response.setHeader(HeaderNameEnum.CONTENT_ENCODING.getName(), HeaderValueEnum.GZIP.getName());
                response.write(data);
            }
        }).route("/b", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                response.setHeader(HeaderNameEnum.CONTENT_ENCODING.getName(), HeaderValueEnum.GZIP.getName());
                response.write(text.getBytes());
            }
        }).route("/c", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws Throwable {
                response.setHeader(HeaderNameEnum.CONTENT_ENCODING.getName(), HeaderValueEnum.GZIP.getName());
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream());
                gzipOutputStream.write("<html><body>hello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello worldhello world".getBytes());
                gzipOutputStream.write("hello world111".getBytes());
                gzipOutputStream.write("</body></html>".getBytes());
                gzipOutputStream.close();
            }
        }).route("/d", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws Throwable {
                String content = "Hello world";
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                gzipOutputStream.write(content.getBytes());
                gzipOutputStream.close();

                byte[] data = outputStream.toByteArray();
                response.setHeader(HeaderNameEnum.CONTENT_ENCODING.getName(), HeaderValueEnum.GZIP.getName());
                response.setContentLength(data.length);
                response.write(data);
            }
        });
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.httpHandler(routeHandle);
        bootstrap.configuration().writeBufferSize(1024 * 1024).debug(true);
        bootstrap.setPort(8080).start();
    }
}
