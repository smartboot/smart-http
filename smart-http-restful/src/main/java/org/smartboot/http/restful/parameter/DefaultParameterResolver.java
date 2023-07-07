package org.smartboot.http.restful.parameter;

import com.alibaba.fastjson2.JSON;
import org.smartboot.http.restful.annotation.Scope;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.socket.util.AttachKey;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author qinluo
 * @date 2023-07-07 15:39:21
 * @since 1.0.0
 */
public class DefaultParameterResolver implements ParameterResolver {

    @Override
    public Object resolve(ParameterMetadata metadata, HttpRequest request, HttpResponse response) {
        Class<?> accepted = metadata.getType();

        if (accepted == HttpRequest.class) {
            return request;
        } else if (accepted == HttpResponse.class) {
            return response;
        }

        String name = metadata.getName();
        Scope scope = metadata.getScope();
        if (scope == null) {
            return DefaultValueResolver.resolve(accepted);
        }

        String origin = null;
        switch (scope) {
            case URL:
                origin = request.getParameter(name);
                break;
            case BODY:
                ByteBuffer buffer;
                if (request.getAttachment() != null
                        && (buffer = request.getAttachment().get(AttachKey.valueOf("bodyBuffer"))) != null) {
                    origin = new String(buffer.array());
                }
                break;
            case HEADER:
                origin = request.getHeader(name);
                break;
        }

        if (origin == null) {
            return DefaultValueResolver.resolve(accepted);
        }

        // convert it.
        Object converted = convert(origin, accepted, metadata.getParameterizedType());
        if (converted == null && accepted.isPrimitive()) {
            return DefaultValueResolver.resolve(accepted);
        }

        return converted;
    }

    private Object convert(String origin, Class<?> accepted, Type genericType) {
        if (accepted == String.class || accepted == Object.class) {
            return origin;
        }

        // For boolean
        if (accepted == Boolean.class || accepted == boolean.class) {
            return Boolean.parseBoolean(origin);
        }

        if (accepted == Character.class || accepted == char.class) {
            return origin.charAt(0);
        }

        // For numbers
        if (Number.class.isAssignableFrom(accepted) || accepted.isPrimitive()) {
            Object resolved = null;
            double numeric = Double.parseDouble(origin);
            if (accepted == Long.class || accepted == long.class) {
                resolved = (long)numeric;
            } else if (accepted == Double.class || accepted == double.class) {
                resolved = numeric;
            } else if (accepted == Integer.class || accepted == int.class) {
                resolved = (int) numeric;
            } else if (accepted == Float.class || accepted == float.class) {
                resolved = (float) numeric;
            } else if (accepted == Short.class || accepted == short.class) {
                resolved = (short) numeric;
            } else if (accepted == Byte.class || accepted == byte.class) {
                resolved = (byte) numeric;
            } else if (accepted == BigDecimal.class) {
                resolved = new BigDecimal(origin);
            }

            return resolved;
        }

        // for pojo object.
        try {
            if (List.class.isAssignableFrom(accepted)) {
                return JSON.parseArray(origin.getBytes()).to(genericType);
            }

            return JSON.parseObject(origin.getBytes()).to(genericType);
        } catch (Exception ignored) {

        }


        return null;
    }

}
