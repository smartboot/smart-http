/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpBootstrap.java
 * Date: 2018-01-28
 * Author: sandao
 */

package org.smartboot.http;

import org.smartboot.http.server.HttpMessageProcessor;
import org.smartboot.http.server.decode.Http11Request;
import org.smartboot.http.server.decode.HttpRequestProtocol;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.utils.NumberUtils;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.extension.ssl.ClientAuth;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSSLQuickServer;

import java.io.IOException;
import java.io.InputStream;

public class HttpBootstrap {

    public static void main(String[] args) {
        HttpMessageProcessor processor = new HttpMessageProcessor(System.getProperty("webapps.dir", "./"));
        processor.route("/", new HttpHandle() {
            byte[] body = ("<html>" +
                    "<head><title>smart-http demo</title></head>" +
                    "<body>" +
                    "GET 表单提交<form action='/get' method='get'><input type='text' name='text'/><input type='submit'/></form></br>" +
                    "POST 表单提交<form action='/post' method='post'><input type='text' name='text'/><input type='submit'/></form></br>" +
                    "文件上传<form action='/upload' method='post' enctype='multipart/form-data'><input type='file' name='text'/><input type='submit'/></form></br>" +
                    "</body></html>").getBytes();

            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {

                response.setContentLength(body.length);
                response.getOutputStream().write(body);
            }
        });
        processor.route("/get", new HttpHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.getOutputStream().write(("收到Get参数text=" + request.getParameter("text")).getBytes());
                response.getOutputStream().flush();
            }
        });
        processor.route("/post", new HttpHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.getOutputStream().write(("收到Post参数text=" + request.getParameter("text")).getBytes());
            }
        });
        processor.route("/upload", new HttpHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                InputStream in = request.getInputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, len);
                }
                in.close();
            }
        });
        http(processor);
        https(processor);
    }

    public static void http(MessageProcessor<Http11Request> processor) {
        // 定义服务器接受的消息类型以及各类消息对应的处理器
        int port = NumberUtils.toInt(System.getProperty("port"), 8080);
        AioQuickServer<Http11Request> server = new AioQuickServer<Http11Request>(port, new HttpRequestProtocol(), processor);
        server.setReadBufferSize(1024);
//        server.setThreadNum(8);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void https(MessageProcessor<Http11Request> processor) {
        // 定义服务器接受的消息类型以及各类消息对应的处理器
        AioSSLQuickServer<Http11Request> server = new AioSSLQuickServer<Http11Request>(8889, new HttpRequestProtocol(), processor);
        server
                .setClientAuth(ClientAuth.OPTIONAL)
                .setKeyStore(ClassLoader.getSystemClassLoader().getResource("server.jks").getFile(), "storepass")
                .setTrust(ClassLoader.getSystemClassLoader().getResource("trustedCerts.jks").getFile(), "storepass")
                .setKeyPassword("keypass");
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
