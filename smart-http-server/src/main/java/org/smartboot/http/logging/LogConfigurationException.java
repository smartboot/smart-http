/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: LogConfigurationException.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.logging;


public class LogConfigurationException extends RuntimeException {


    private static final long serialVersionUID = 1L;


    public LogConfigurationException() {
        super();
    }


    public LogConfigurationException(String message) {
        super(message);
    }


    public LogConfigurationException(Throwable cause) {
        super(cause);
    }


    public LogConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
