package org.smartboot.http.restful.intercept;

import org.smartboot.http.server.HttpRequest;

import java.lang.reflect.Method;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/1/25
 */
public final class MethodInvocationImpl implements MethodInvocation {
    private final Method method;
    private final Object[] args;
    private final Object object;

    public MethodInvocationImpl(Method method, Object[] args, Object object) {
        this.method = method;
        this.args = args;
        this.object = object;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return args;
    }

    @Override
    public Object getThis() {
        return object;
    }

    @Override
    public Object proceed() throws Throwable {
        return method.invoke(object, args);
    }

}
