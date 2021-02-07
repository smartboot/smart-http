/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRouteDemo.java
 * Date: 2020-04-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.example;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.handle.HttpRouteHandle;

import java.io.IOException;

/**
 * 请求路由示例
 *
 * @author 三刀
 * @version V1.0 , 2020/4/1
 */
public class HttpRouteDemo {
    public static void main(String[] args) {
        //1. 实例化路由Handle
        HttpRouteHandle routeHandle = new HttpRouteHandle();

        //2. 指定路由规则以及请求的处理实现
        routeHandle.route("/", new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.write("smart-http".getBytes());
            }
        }).route("/test1", new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.write(("test1").getBytes());
            }
        }).route("/test2", new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.write(("test2").getBytes());
            }
        });

        // 3. 启动服务
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(routeHandle);
        bootstrap.start();
    }
}
