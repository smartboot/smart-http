package org.smartboot.http.server.test;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.HttpMessageProcessor;
import org.smartboot.http.server.handle.HttpHandle;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public class SmartHttpDemo {
    public static void main(String[] args) {
        System.setProperty("smart-socket.server.pageSize", (1024 * 1024 * 5) + "");
        System.setProperty("smart-socket.session.writeChunkSize", (1024 * 4) + "");

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
        processor.route("/plaintext", new HttpHandle() {
            byte[] body = "Hello World!".getBytes();

            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.setContentLength(body.length);
                response.getOutputStream().write(body);
            }
        });
        HttpBootstrap bootstrap = new HttpBootstrap(processor);
        bootstrap.setThreadNum(Runtime.getRuntime().availableProcessors() + 2)
                .setPort(8080)
                .start();
    }
}
