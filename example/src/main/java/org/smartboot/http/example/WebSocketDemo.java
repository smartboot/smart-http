/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketDemo.java
 * Date: 2020-04-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.example;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.WebSocketRequest;
import org.smartboot.http.WebSocketResponse;
import org.smartboot.http.server.handle.WebSocketDefaultHandle;
import org.smartboot.http.server.handle.WebSocketRouteHandle;

/**
 * @author 三刀
 * @version V1.0 , 2020/4/1
 */
public class WebSocketDemo {
    public static void main(String[] args) {
        //1. 实例化路由Handle
        WebSocketRouteHandle routeHandle = new WebSocketRouteHandle();

        //2. 指定路由规则以及请求的处理实现
        routeHandle.route("/", new WebSocketDefaultHandle() {
            @Override
            public void handleTextMessage(WebSocketRequest request, WebSocketResponse response, String data) {
                response.sendTextMessage("接受到客户端消息：" + data);
            }
        });

        // 3. 启动服务
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.wsPipeline().next(routeHandle);
        bootstrap.start();
    }
}
