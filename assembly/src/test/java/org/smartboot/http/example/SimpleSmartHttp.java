/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: SimpleSmartHttp.java
 * Date: 2020-04-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.example;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.handle.WebSocketDefaultHandle;

import java.io.IOException;

/**
 * 打开浏览器请求：http://127.0.0.0:8080/
 *
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public class SimpleSmartHttp {
    public static void main(String[] args) {
        HttpBootstrap bootstrap = new HttpBootstrap();
        // 普通http请求
        bootstrap.pipeline().next(new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.write("hello world<br/>".getBytes());
                for (Cookie cookie : request.getCookies()) {
                    response.write(("<br/>cookie : " + cookie).getBytes());
                }
            }
        });
        // websocket请求
        bootstrap.wsPipeline().next(new WebSocketDefaultHandle() {
            @Override
            public void handleTextMessage(WebSocketRequest request, WebSocketResponse response, String data) {
                response.sendTextMessage("Hello World");
            }
        });
        bootstrap.setPort(8080).start();
    }
}
