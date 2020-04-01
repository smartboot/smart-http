/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: FileSmartHttp.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.example;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.server.handle.HttpStaticResourceHandle;

/**
 * 打开浏览器请求：http://127.0.0.0:8080/
 *
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public class FileSmartHttp {
    public static void main(String[] args) {
        String webdir = System.getProperty("user.dir") + "/example/webapps";
        HttpBootstrap bootstrap = new HttpBootstrap();
        //配置HTTP消息处理管道
        bootstrap.pipeline().next(new HttpStaticResourceHandle(webdir));

        //设定服务器配置并启动
        bootstrap.setPort(8080).start();
    }
}
