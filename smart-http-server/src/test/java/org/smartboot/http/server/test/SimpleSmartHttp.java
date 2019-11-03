package org.smartboot.http.server.test;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.handle.HttpHandle;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public class SimpleSmartHttp {
    public static void main(String[] args) {
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(new HttpHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.write("hello world".getBytes());
            }
        });
        bootstrap.setPort(8080).start();
    }
}
