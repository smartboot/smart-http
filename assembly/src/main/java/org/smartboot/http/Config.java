/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Config.java
 * Date: 2020-04-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http;

import org.smartboot.http.utils.Param;

/**
 * @author 三刀
 * @version V1.0 , 2020/4/3
 */
public class Config {
    @Param
    private int port;
    @Param
    private String webapps;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getWebapps() {
        return webapps;
    }

    public void setWebapps(String webapps) {
        this.webapps = webapps;
    }

    @Override
    public String toString() {
        return "Config{" +
                "port=" + port +
                ", webapps='" + webapps + '\'' +
                '}';
    }
}
