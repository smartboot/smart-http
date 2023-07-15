/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketDemo.java
 * Date: 2021-06-20
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.demo;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.handler.WebSocketDefaultHandler;
import org.smartboot.http.server.handler.WebSocketRouteHandler;

/**
 * @author 三刀
 * @version V1.0 , 2020/4/1
 */
public class WebSocketDemo {
    public static void main(String[] args) {
        //1. 实例化路由Handle
        WebSocketRouteHandler routeHandle = new WebSocketRouteHandler();

        //2. 指定路由规则以及请求的处理实现
        routeHandle.route("/", new WebSocketDefaultHandler() {
            @Override
            public void handleTextMessage(WebSocketRequest request, WebSocketResponse response, String data) {
                response.ping("hello".getBytes());
                response.sendTextMessage("接受到客户端消息：" + data);
            }
        });

        // 3. 启动服务
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.webSocketHandler(routeHandle);
        bootstrap.start();
    }
}
