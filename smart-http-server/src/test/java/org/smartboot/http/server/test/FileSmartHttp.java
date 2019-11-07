package org.smartboot.http.server.test;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.server.handle.StaticResourceHandle;

/**
 * 打开浏览器请求：http://127.0.0.0:8080/
 *
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public class FileSmartHttp {
    public static void main(String[] args) {
        String webdir = System.getProperty("user.dir") + "/smart-http-server/webapps";
        HttpBootstrap bootstrap = new HttpBootstrap();
        //配置HTTP消息处理管道
        bootstrap.pipeline().next(new StaticResourceHandle(webdir));

        //设定服务器配置并启动
        bootstrap.setPort(8080).start();
    }
}
