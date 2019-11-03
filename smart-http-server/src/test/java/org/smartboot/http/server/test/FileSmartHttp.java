package org.smartboot.http.server.test;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.server.handle.RouteHandle;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public class FileSmartHttp {
    public static void main(String[] args) {
        String webdir = System.getProperty("user.dir") + "/smart-http-server/webapps";
        HttpBootstrap bootstrap = new HttpBootstrap();
        //配置HTTP消息处理管道
        bootstrap.pipeline().next(new RouteHandle(webdir));

        //设定服务器配置并启动
        bootstrap.setPort(8080).start();
    }
}
