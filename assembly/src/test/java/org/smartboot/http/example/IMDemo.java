/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: IMDemo.java
 * Date: 2020-05-10
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.example;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.WebSocketRequest;
import org.smartboot.http.WebSocketResponse;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.server.handle.HttpRouteHandle;
import org.smartboot.http.server.handle.WebSocketDefaultHandle;
import org.smartboot.http.server.handle.WebSocketRouteHandle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2020/5/10
 */
public class IMDemo {
    public static void main(String[] args) {
        HttpRouteHandle routeHandle = new HttpRouteHandle();
        routeHandle.route("/", new HttpHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                OutputStream writeBuffer = response.getOutputStream();
                InputStream inputStream = IMDemo.class.getClassLoader().getResourceAsStream("im.html");
                byte[] bytes = new byte[1024];
                int length = 0;
                while ((length = inputStream.read(bytes)) != -1) {
                    writeBuffer.write(bytes, 0, length);
                }
            }
        });

        WebSocketRouteHandle webSocketRouteHandle = new WebSocketRouteHandle();
        webSocketRouteHandle.route("/", new WebSocketDefaultHandle() {
            private Map<WebSocketRequest, WebSocketResponse> sessionMap = new ConcurrentHashMap<>();

            @Override
            public void handleTextMessage(WebSocketRequest request, WebSocketResponse response, String data) {
                sessionMap.values().forEach(rsp -> {
                    rsp.sendTextMessage(data);
                    rsp.flush();
                });
            }

            @Override
            public void onHandShark(WebSocketRequest request, WebSocketResponse response) {
                System.out.println("加入群组 session");
                sessionMap.put(request, response);
            }

            @Override
            public void onClose(WebSocketRequest request, WebSocketResponse response) {
                System.out.println("移除群组");
                sessionMap.remove(request);
            }
        });
        HttpBootstrap bootstrap = new HttpBootstrap();
        //配置HTTP消息处理管道
        bootstrap.pipeline().next(routeHandle);
        bootstrap.wsPipeline().next(webSocketRouteHandle);

        //设定服务器配置并启动
        bootstrap.setPort(8080).start();
    }
}
