package org.smartboot.http.server;

import java.io.IOException;

/**
 * @author huqiang
 * @since 2021/3/2 14:55
 */
public class HttpBootstrapTest {

    public static void main(String[] args) {
        HttpBootstrap bootstrap = new HttpBootstrap().httpHandler(new HttpServerHandler() {
            byte[] bytes = "hello world".getBytes();

            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
//                System.out.println("url:"+request.getRequestURL());
//                System.out.println("param:"+request.getParameters());
//                System.out.println("name: "+request.getParameter("name"));
                response.setContentLength(bytes.length);
                response.setContentType("text/plain; charset=UTF-8");
                response.write(bytes);
            }
        }).setPort(8080);
        bootstrap.configuration()
                .threadNum(Runtime.getRuntime().availableProcessors())
                .readBufferSize(1024 * 4)
                .writeBufferSize(1024 * 4)
                .readMemoryPool(16384 * 1024 * 4)
                .writeMemoryPool(10 * 1024 * 1024 * Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors()).debug(false);
        bootstrap.start();
    }
}
