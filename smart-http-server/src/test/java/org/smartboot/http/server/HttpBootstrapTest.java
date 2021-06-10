package org.smartboot.http.server;

import java.io.IOException;

/**
 * @author huqiang
 * @since 2021/3/2 14:55
 */
public class HttpBootstrapTest {

    public static void main(String[] args) {
        new HttpBootstrap().pipeline(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                System.out.println("url:"+request.getRequestURL());
                System.out.println("param:"+request.getParameters());
                System.out.println("name: "+request.getParameter("name"));
                response.write("hello world".getBytes());
            }
        }).setPort(8080).start();
    }
}
