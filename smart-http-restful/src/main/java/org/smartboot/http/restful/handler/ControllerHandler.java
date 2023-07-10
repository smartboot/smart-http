package org.smartboot.http.restful.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.restful.annotation.Param;
import org.smartboot.http.restful.intercept.MethodInterceptor;
import org.smartboot.http.restful.intercept.MethodInvocation;
import org.smartboot.http.restful.intercept.MethodInvocationImpl;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.util.AttachKey;
import org.smartboot.socket.util.Attachment;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

class ControllerHandler extends HttpServerHandler {
    private static final AttachKey<ByteBuffer> jsonBodyBufferKey = AttachKey.valueOf("bodyBuffer");
    private final Method method;
    private final Object controller;
    private final MethodInterceptor interceptor;
    private final BiConsumer<HttpRequest, HttpResponse> inspect;
    private final ParamInvoker[] paramInvokers;
    private boolean needContext;

    public ControllerHandler(Method method, Object controller, BiConsumer<HttpRequest, HttpResponse> inspect, MethodInterceptor interceptor) {
        this.method = method;
        this.controller = controller;
        this.inspect = inspect;
        this.interceptor = interceptor;
        Parameter[] parameters = method.getParameters();
        paramInvokers = new ParamInvoker[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Type type = parameter.getType();

            if (type == HttpRequest.class) {
                paramInvokers[i] = ParamInvoker.HttpRequestHttpRequest;
                continue;
            }
            if (type == HttpResponse.class) {
                paramInvokers[i] = ParamInvoker.HttpResponseHttpRequest;
                continue;
            }
            Param param = parameter.getAnnotation(Param.class);
            //param为null，说明是对象转换
            if (param == null) {
                if (type.getTypeName().startsWith("java")) {
                    throw new IllegalArgumentException();
                }
                paramInvokers[i] = (request, response, context) -> context.getJsonObject().to(type);
            } else {
                paramInvokers[i] = (request, response, context) -> context.getJsonObject().getObject(param.value(), type);
            }
            needContext = true;
        }
    }

    @Override
    public boolean onBodyStream(ByteBuffer buffer, Request request) {
        Attachment attachment = request.getAttachment();
        ByteBuffer bodyBuffer = null;
        if (attachment != null) {
            bodyBuffer = attachment.get(jsonBodyBufferKey);
        }
        if (bodyBuffer != null || request.getContentLength() > 0 && request.getContentType().startsWith("application/json")) {
            if (bodyBuffer == null) {
                bodyBuffer = ByteBuffer.allocate(request.getContentLength());
                if (attachment == null) {
                    attachment = new Attachment();
                    request.setAttachment(attachment);
                }
                attachment.put(jsonBodyBufferKey, bodyBuffer);
            }
            if (buffer.remaining() <= bodyBuffer.remaining()) {
                bodyBuffer.put(buffer);
            } else {
                int limit = buffer.limit();
                buffer.limit(buffer.position() + bodyBuffer.remaining());
                bodyBuffer.put(buffer);
                buffer.limit(limit);
            }
            return !bodyBuffer.hasRemaining();
        } else {
            return super.onBodyStream(buffer, request);
        }
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        try {
            Object[] params = getParams(request, response);
            method.setAccessible(true);
            MethodInvocation invocation = new MethodInvocationImpl(method, params, controller);
            inspect.accept(request, response);
            Object rsp = interceptor.invoke(invocation);
//            Object rsp = method.invoke(controller, params);
            if (rsp != null) {
                byte[] bytes;
                if (rsp instanceof String) {
                    bytes = ((String) rsp).getBytes();
                } else {
                    response.setHeader(HeaderNameEnum.CONTENT_TYPE.getName(), HeaderValueEnum.APPLICATION_JSON.getName());
                    bytes = JSON.toJSONBytes(rsp);
                }
                //如果在controller中已经触发过write，此处的contentLength将不准，且不会生效
                response.setContentLength(bytes.length);
                response.write(bytes);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] getParams(HttpRequest request, HttpResponse response) {
        Object[] params = new Object[paramInvokers.length];

        InvokerContext context = null;
        if (needContext) {
            JSONObject jsonObject;
            Attachment attachment = request.getAttachment();
            ByteBuffer bodyBuffer = null;
            if (attachment != null) {
                bodyBuffer = attachment.get(jsonBodyBufferKey);
            }
            if (bodyBuffer != null) {
                jsonObject = JSON.parseObject(bodyBuffer.array());
            } else {
                jsonObject = new JSONObject();
                request.getParameters().keySet().forEach(param -> {
                    jsonObject.put(param, request.getParameter(param));
                });
            }
            context = new InvokerContext();
            context.setJsonObject(jsonObject);
        }
        for (int i = 0; i < params.length; i++) {
            params[i] = paramInvokers[i].invoker(request, response, context);
        }
        return params;
    }

    public interface ParamInvoker {
        ParamInvoker HttpRequestHttpRequest = (request, response, context) -> request;

        ParamInvoker HttpResponseHttpRequest = (request, response, context) -> response;

        Object invoker(HttpRequest request, HttpResponse response, InvokerContext context);

    }

    public static class InvokerContext {
        private JSONObject jsonObject;

        public JSONObject getJsonObject() {
            return jsonObject;
        }

        public void setJsonObject(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

    }
}
