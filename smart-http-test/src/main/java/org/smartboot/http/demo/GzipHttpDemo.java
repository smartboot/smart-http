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

import java.io.IOException;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/10/24
 */
public class GzipHttpDemo {
    public static void main(String[] args) {
        HttpRouteHandler routeHandle = new HttpRouteHandler();
        routeHandle.route("/a", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                byte[] data = "hello smart-http<br/>".getBytes();
                response.setContentLength(data.length);
                response.setHeader(HeaderNameEnum.CONTENT_ENCODING.getName(), HeaderValueEnum.GZIP.getName());
                response.write(data);
            }
        }).route("/b", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                response.setHeader(HeaderNameEnum.CONTENT_ENCODING.getName(), HeaderValueEnum.GZIP.getName());
                response.write("hello smart-http<br/>".getBytes());
            }
        });
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.httpHandler(routeHandle);
        bootstrap.configuration().debug(true);
        bootstrap.setPort(8080).start();
    }
}
