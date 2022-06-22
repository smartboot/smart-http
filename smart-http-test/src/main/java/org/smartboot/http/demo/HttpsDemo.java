/*******************************************************************************
 * Copyright (c) 2017-2022, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpsDemo.java
 * Date: 2022-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.demo;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.plugins.StreamMonitorPlugin;
import org.smartboot.socket.extension.ssl.ClientAuth;
import org.smartboot.socket.extension.ssl.factory.ServerSSLContextFactory;

import java.io.IOException;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/2/4
 */
public class HttpsDemo {
    public static void main(String[] args) throws Exception {
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                response.write("hello smart-http<br/>".getBytes());
            }
        });
        SslPlugin sslPlugin=new SslPlugin(new ServerSSLContextFactory(HttpsDemo.class.getClassLoader().getResourceAsStream("server.keystore"), "123456", "123456"),ClientAuth.NONE);
        bootstrap.configuration()
                .addPlugin(sslPlugin)
                .addPlugin(new StreamMonitorPlugin<>())
                .debug(false);
        bootstrap.setPort(8080).start();
    }
}
