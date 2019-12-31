/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Logger.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.logging;

public interface Logger {


    public boolean isDebugEnabled();


    public boolean isErrorEnabled();


    public boolean isFatalEnabled();


    public boolean isInfoEnabled();


    public boolean isTraceEnabled();


    public boolean isWarnEnabled();

    public void trace(Object message);

    public void trace(Object message, Throwable t);


    public void debug(Object message);


    public void debug(Object message, Throwable t);

    public void info(Object message);

    public void info(Object message, Throwable t);


    public void warn(Object message);


    public void warn(Object message, Throwable t);


    public void error(Object message);


    public void error(Object message, Throwable t);

    public void fatal(Object message);


    public void fatal(Object message, Throwable t);


}
