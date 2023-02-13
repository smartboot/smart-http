/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpPost.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.QueueUnit;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.socket.transport.AioSession;

import java.net.URLEncoder;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/4
 */
public final class HttpPost extends HttpRest {

    HttpPost(String uri, String host, AioSession session, AbstractQueue<QueueUnit> queue) {
        super(uri, host, session, queue);
        request.setMethod(HttpMethodEnum.POST.getMethod());
    }

    @Override
    public HttpRest setMethod(String method) {
        throw new UnsupportedOperationException();
    }

    public void sendForm(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            super.send();
            return;
        }
        try {
            willSendRequest();
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
            request.addHeader(HeaderNameEnum.CONTENT_LENGTH.getName(), String.valueOf(bytes.length));
            request.addHeader(HeaderNameEnum.CONTENT_TYPE.getName(), HeaderValueEnum.X_WWW_FORM_URLENCODED.getName());
            //输出数据
            request.write(bytes);
            request.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            completableFuture.completeExceptionally(e);
        }
    }

    @Override
    public BodyStream<HttpPost> bodyStream() {
        BodyStream<? extends HttpRest> superBodyStream = super.bodyStream();
        return new BodyStream<HttpPost>() {
            @Override
            public BodyStream<HttpPost> write(byte[] bytes, int offset, int len) {
                superBodyStream.write(bytes, offset, len);
                return this;
            }

            @Override
            public BodyStream<HttpPost> flush() {
                superBodyStream.flush();
                return this;
            }

            @Override
            public HttpPost finish() {
                superBodyStream.flush();
                return HttpPost.this;
            }
        };
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

    @Override
    public Header<HttpPost> header() {
        return new HeaderWrapper<>(this, super.header());
    }
}
