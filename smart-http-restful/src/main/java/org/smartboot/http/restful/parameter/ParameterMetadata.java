package org.smartboot.http.restful.parameter;

import org.smartboot.http.restful.annotation.Scope;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author qinluo
 * @date 2023-07-07 15:33:21
 * @since 1.2.7
 */
public class ParameterMetadata implements Serializable {

    private static final long serialVersionUID = 3946866919713892561L;

    private Class<?> type;
    private String name;
    private Scope scope;
    private Method method;
    private Type parameterizedType;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Type getParameterizedType() {
        return parameterizedType;
    }

    public void setParameterizedType(Type parameterizedType) {
        this.parameterizedType = parameterizedType;
    }
}
