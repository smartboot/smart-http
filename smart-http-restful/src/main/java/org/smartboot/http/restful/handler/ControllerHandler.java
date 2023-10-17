package org.smartboot.http.restful.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.util.Streams;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.restful.annotation.Param;
import org.smartboot.http.restful.fileupload.MultipartFile;
import org.smartboot.http.restful.fileupload.SmartHttpFileUpload;
import org.smartboot.http.restful.intercept.MethodInterceptor;
import org.smartboot.http.restful.intercept.MethodInvocation;
import org.smartboot.http.restful.intercept.MethodInvocationImpl;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class ControllerHandler extends HttpServerHandler {
    private final Method method;
    private final Object controller;
    private final MethodInterceptor interceptor;
    private final ParamInvoker[] paramInvokers;
    private boolean needContext;

    public ControllerHandler(Method method, Object controller, MethodInterceptor interceptor) {
        this.method = method;
        this.controller = controller;
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
            needContext = true;

            Param param = parameter.getAnnotation(Param.class);
            if (param != null) {
                paramInvokers[i] = (request, response, context) -> {
                    if (type == MultipartFile.class) {
                        return context.files.get(param.value());
                    }
                    return context.getJsonObject().getObject(param.value(), type);
                };
                continue;
            }
            //param为null，说明是对象转换
            if (Collection.class.isAssignableFrom(parameter.getType())) {
                paramInvokers[i] = (request, response, context) -> context.getJsonArray().to(type);
                continue;
            }
            if (type.getTypeName().startsWith("java")) {
                throw new IllegalArgumentException();
            }
            paramInvokers[i] = (request, response, context) -> context.getJsonObject().to(type);


        }
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Throwable {
        Object[] params = getParams(request, response);
        method.setAccessible(true);
        MethodInvocation invocation = new MethodInvocationImpl(method, params, controller, request, response);
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
    }

    private Object[] getParams(HttpRequest request, HttpResponse response) throws IOException, FileUploadException {
        Object[] params = new Object[paramInvokers.length];

        InvokerContext context = null;
        if (needContext) {
            context = new InvokerContext();
            Object object;
            if (request.getContentType() != null && request.getContentType().startsWith("application/json")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int len = 0;
                InputStream inputStream = request.getInputStream();
                while ((len = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, len);
                }
                object = JSON.parse(out.toByteArray());
            } else if (request.getContentType() != null && request.getContentType().startsWith(HeaderValueEnum.MULTIPART_FORM_DATA.getName())) {
                Map<String, MultipartFile> files = new HashMap<>();
                SmartHttpFileUpload upload = new SmartHttpFileUpload();
                JSONObject jsonObject = new JSONObject();
                FileItemIterator iterator = upload.getItemIterator(request);
                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    InputStream stream = item.openStream();
                    if (item.isFormField()) {
                        jsonObject.put(item.getFieldName(), Streams.asString(stream));
                    } else {
                        files.put(item.getFieldName(), new MultipartFile(item.getFieldName(), item.openStream()));
                    }
                }
                context.files = files;
                object = jsonObject;
            } else {
                JSONObject jsonObject = new JSONObject();
                request.getParameters().keySet().forEach(param -> {
                    jsonObject.put(param, request.getParameter(param));
                });
                object = jsonObject;
            }

            context.setJsonObject(object);
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
        private Object jsonObject;

        private Map<String, MultipartFile> files;

        public JSONObject getJsonObject() {
            return (JSONObject) jsonObject;
        }

        public JSONArray getJsonArray() {
            return (JSONArray) jsonObject;
        }

        public void setJsonObject(Object jsonObject) {
            this.jsonObject = jsonObject;
        }

    }
}
