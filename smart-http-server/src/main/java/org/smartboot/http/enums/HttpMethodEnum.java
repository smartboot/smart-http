/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: MethodEnum.java
 * Date: 2018-02-06
 * Author: sandao
 */

package org.smartboot.http.enums;

/**
 * Http支持的Method
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public enum HttpMethodEnum {
    OPTIONS("OPTIONS"),
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    TRACE("TRACE"),
    CONNECT("CONNECT");

    private String method;

    HttpMethodEnum(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
