/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpException.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.exception;

import org.smartboot.http.common.enums.HttpStatus;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/3
 */
public class HttpException extends RuntimeException {
    private int httpCode;

    private String desc;

    public HttpException(HttpStatus httpStatus) {
        this(httpStatus, httpStatus.getReasonPhrase());
    }

    public HttpException(HttpStatus httpStatus, String desc) {
        super(httpStatus.getReasonPhrase());
        this.httpCode = httpStatus.value();
        this.desc = desc;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
