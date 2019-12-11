package org.smartboot.servlet.example;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.servlet.ServletHttpHandle;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletDemo {
    public static void main(String[] args) {
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(new ServletHttpHandle());
        bootstrap.setPort(8080).start();
    }
}
