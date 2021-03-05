/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpPost.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.socket.transport.WriteBuffer;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/4
 */
public class HttpPost extends HttpRest {

    public HttpPost(String uri, String host, WriteBuffer writeBuffer, Consumer<CompletableFuture<HttpResponse>> bindListener) {
        super(uri, host, writeBuffer, bindListener);
        request.setMethod(HttpMethodEnum.POST.getMethod());
    }

    @Override
    public HttpRest setMethod(String method) {
        throw new UnsupportedOperationException();
    }

    public HttpPost sendForm(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            super.send();
            return this;
        }
        try {
            bindResponseListener();
            //编码Post表单
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            Map.Entry<String, String> entry = iterator.next();
            StringBuilder sb = new StringBuilder();
            sb.append(URLEncoder.encode(entry.getKey(), "utf8")).append("=").append(URLEncoder.encode(entry.getValue(), "utf8"));
            while (iterator.hasNext()) {
                entry = iterator.next();
                sb.append("&").append(URLEncoder.encode(entry.getKey(), "utf8")).append("=").append(URLEncoder.encode(entry.getValue(), "utf8"));
            }
            byte[] bytes = sb.toString().getBytes();
            // 设置 Header
            addHeader(HttpHeaderConstant.Names.CONTENT_LENGTH, String.valueOf(bytes.length));
            addHeader(HttpHeaderConstant.Names.CONTENT_TYPE, HttpHeaderConstant.Values.X_WWW_FORM_URLENCODED);
            //输出数据
            request.write(bytes);
            request.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            completableFuture.completeExceptionally(e);
        }
        return this;
    }

    @Override
    public HttpPost addHeader(String headerName, String headerValue) {
        super.addHeader(headerName, headerValue);
        return this;
    }

    @Override
    public HttpPost onSuccess(Consumer<HttpResponse> consumer) {
        super.onSuccess(consumer);
        return this;
    }

    @Override
    public HttpPost onFailure(Consumer<Throwable> consumer) {
        super.onFailure(consumer);
        return this;
    }

    public HttpPost setContentType(String contentType) {
        request.setContentType(contentType);
        return this;
    }

    @Override
    public HttpPost send() {
        super.send();
        return this;
    }
}
