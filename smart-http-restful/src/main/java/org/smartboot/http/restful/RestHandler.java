package org.smartboot.http.restful;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;
import org.smartboot.http.restful.intercept.MethodInterceptor;
import org.smartboot.http.restful.intercept.MethodInvocation;
import org.smartboot.http.restful.intercept.MethodInvocationImpl;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.handler.HttpRouteHandler;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.util.AttachKey;
import org.smartboot.socket.util.Attachment;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
class RestHandler extends HttpServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestHandler.class);
    private final HttpRouteHandler httpRouteHandler;
    private AttachKey<ByteBuffer> bodyBufferKey = AttachKey.valueOf("bodyBuffer");
    private BiConsumer<HttpRequest, HttpResponse> inspect = (httpRequest, response) -> {
    };
    private final MethodInterceptor interceptor = MethodInvocation::proceed;


    public RestHandler(HttpServerHandler defaultHandler) {
        this.httpRouteHandler = defaultHandler != null ? new HttpRouteHandler(defaultHandler) : new HttpRouteHandler();
    }

    public void addController(Object object) {
        Class<?> clazz = object.getClass();
        Controller controller = clazz.getDeclaredAnnotation(Controller.class);
        String rootPath = controller.value();
        System.out.println(object + "method:" + clazz.getDeclaredMethods().length);
        for (Method method : clazz.getDeclaredMethods()) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping == null) {
                continue;
            }
            String mappingUrl = getMappingUrl(rootPath, requestMapping);

            httpRouteHandler.route(mappingUrl, new HttpServerHandler() {
                @Override
                public boolean onBodyStream(ByteBuffer buffer, Request request) {
                    Attachment attachment = request.getAttachment();
                    ByteBuffer bodyBuffer = null;
                    if (attachment != null) {
                        bodyBuffer = attachment.get(bodyBufferKey);
                    }
                    if (bodyBuffer != null || request.getContentLength() > 0 && request.getContentType().startsWith("application/json")) {
                        if (bodyBuffer == null) {
                            bodyBuffer = ByteBuffer.allocate(request.getContentLength());
                            if (attachment == null) {
                                attachment = new Attachment();
                                request.setAttachment(attachment);
                            }
                            attachment.put(bodyBufferKey, bodyBuffer);
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
                        Type[] types = method.getGenericParameterTypes();
                        Object[] params = new Object[types.length];
                        Attachment attachment = request.getAttachment();
                        ByteBuffer bodyBuffer = null;
                        if (attachment != null) {
                            bodyBuffer = attachment.get(bodyBufferKey);
                        }

                        for (int i = 0; i < types.length; i++) {
                            Type type = types[i];
                            if (type == HttpRequest.class) {
                                params[i] = request;
                            } else if (type == HttpResponse.class) {
                                params[i] = response;
                            } else if (!type.getTypeName().startsWith("java")) {
                                JSONObject jsonObject;
                                if (bodyBuffer != null) {
                                    jsonObject = JSON.parseObject(bodyBuffer.array());
                                } else {
                                    jsonObject = new JSONObject();
                                    request.getParameters().keySet().forEach(param -> {
                                        jsonObject.put(param, request.getParameter(param));
                                    });
                                }
                                params[i] = jsonObject.to(type);
                            } else {
                                System.out.println("aaaaaa......");
                            }
                        }
                        method.setAccessible(true);
                        MethodInvocation invocation = new MethodInvocationImpl(method, params, object);
                        inspect.accept(request, response);
                        Object rsp = interceptor.invoke(invocation);
//                        Object rsp = method.invoke(object, params);
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
            });
        }
    }


    private String getMappingUrl(String rootPath, RequestMapping requestMapping) {
        StringBuilder sb = new StringBuilder("/");
        if (rootPath.length() > 0) {
            if (rootPath.charAt(0) == '/') {
                sb.append(rootPath, 1, rootPath.length());
            } else {
                sb.append(rootPath);
            }
        }
        if (requestMapping.value().length() > 0) {
            char sbChar = sb.charAt(sb.length() - 1);
            if (requestMapping.value().charAt(0) == '/') {
                if (sbChar == '/') {
                    sb.append(requestMapping.value(), 1, requestMapping.value().length());
                } else {
                    sb.append(requestMapping.value());
                }
            } else {
                if (sbChar != '/') {
                    sb.append('/');
                }
                sb.append(requestMapping.value());
            }
        }
        return sb.toString();
    }

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        httpRouteHandler.onHeaderComplete(request);
    }

    public void setInspect(BiConsumer<HttpRequest, HttpResponse> inspect) {
        this.inspect = inspect;
    }


}
