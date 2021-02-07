/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: SimpleSmartHttp.java
 * Date: 2020-04-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.ParamReflect;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handle.HttpStaticResourceHandle;

/**
 * 打开浏览器请求：http://127.0.0.0:8080/
 *
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public class BootStrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootStrap.class);
    private static final String PROPERTY_CONFIG = "http_config";


    public static void main(String[] args) {
        Config config = new Config();
        ParamReflect.reflect(System.getProperty(PROPERTY_CONFIG), config);
        HttpBootstrap bootstrap = new HttpBootstrap();
        //配置HTTP消息处理管道
        bootstrap.pipeline().next(new HttpStaticResourceHandle(config.getWebapps()));

        //设定服务器配置并启动
        bootstrap.host(config.getHost()).setPort(config.getPort()).start();
        LOGGER.info("start smart-http http://{}:{}", config.getHost(), config.getPort());
    }


}
