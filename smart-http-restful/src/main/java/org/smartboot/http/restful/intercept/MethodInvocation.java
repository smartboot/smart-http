package org.smartboot.http.restful.intercept;

import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;

import java.lang.reflect.Method;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/1/25
 */
public interface MethodInvocation {
    Method getMethod();

    Object[] getArguments();

    Object getThis();

    Object proceed() throws Throwable;

    HttpRequest request();

    HttpResponse response();
}
