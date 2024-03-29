/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: SimpleSmartHttp.java
 * Date: 2021-06-08
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.demo;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.io.InputStream;


public class SimpleSmartHttp {
    public static void main(String[] args) {
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.configuration().debug(true);
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                InputStream in = request.getInputStream();
                byte[] b = new byte[1024];
                int i;
                while ((i = in.read(b)) > 0) {
                    System.out.println(new String(b, 0, i));
                }
                response.write("hello smart-http<br/>".getBytes());
            }
        }).setPort(8080).start();
    }
}