package org.smartboot.http.graal;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handler.HttpStaticResourceHandler;

/**
 * mvn -Pnative -DskipTests package
 */
public class Bootstrap {
    public static void main(String[] args) {
        //解析 -dir 参数
        String dir = "";
        for (int i = 0; i < args.length; i++) {
            if ("--dir".equals(args[i])) {
                dir = args[++i];
            }
        }

        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.configuration().debug(true);
        bootstrap.httpHandler(new HttpStaticResourceHandler(dir)).setPort(8080).start();
    }
}