/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRest.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.DefaultHttpResponseHandler;
import org.smartboot.http.client.impl.HttpRequestImpl;
import org.smartboot.http.client.impl.HttpResponseImpl;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/3
 */
public class HttpRest implements IHttpRest {
    private final static String DEFAULT_USER_AGENT = "smart-http";
    protected final HttpRequestImpl request;
    protected final CompletableFuture<HttpResponseImpl> completableFuture = new CompletableFuture<>();
    private final AbstractQueue<AbstractResponse> queue;
    private Map<String, String> queryParams = null;
    private boolean commit = false;
    private Body<HttpRest> body;
    /**
     * http body 解码器
     */
    private ResponseHandler responseHandler = new DefaultHttpResponseHandler();
    private final HttpResponseImpl response;
    private final Semaphore semaphore;

    HttpRest(AioSession session, AbstractQueue<AbstractResponse> queue, Semaphore semaphore) {
        this.request = new HttpRequestImpl(session);
        this.queue = queue;
        this.response = new HttpResponseImpl(session, completableFuture);
        this.semaphore = semaphore;
    }

    protected final void willSendRequest() {
        if (commit) {
            return;
        }
        commit = true;
        resetUri();
        Collection<String> headers = request.getHeaderNames();
        if (!headers.contains(HeaderNameEnum.USER_AGENT.getName())) {
            request.addHeader(HeaderNameEnum.USER_AGENT.getName(), DEFAULT_USER_AGENT);
        }
        response.setResponseHandler(responseHandler);
        AioSession session = response.getSession();
        ResponseAttachment attachment = session.getAttachment();
        synchronized (session) {
            if (attachment.getResponse() == null) {
                attachment.setResponse(response);
            } else {
                queue.offer(response);
            }
        }
    }

    private void resetUri() {
        if (queryParams == null) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(request.getUri());
        int index = request.getUri().indexOf("#");
        if (index > 0) {
            stringBuilder.setLength(index);
        }
        index = request.getUri().indexOf("?");
        if (index == -1) {
            stringBuilder.append('?');
        } else if (index < stringBuilder.length() - 1) {
            stringBuilder.append('&');
        }
        queryParams.forEach((key, value) -> {
            try {
                stringBuilder.append(key).append('=').append(URLEncoder.encode(value, "utf8")).append('&');
            } catch (UnsupportedEncodingException e) {
                stringBuilder.append(key).append('=').append(value).append('&');
            }
        });
        if (stringBuilder.length() > 0) {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        request.setUri(stringBuilder.toString());
    }

    public Body<? extends HttpRest> body() {
        if (body == null) {
            body = new Body<HttpRest>() {

                @Override
                public Body<HttpRest> write(byte[] bytes, int offset, int len) {
                    try {
                        willSendRequest();
                        request.getOutputStream().write(bytes, offset, len);
                    } catch (IOException e) {
                        System.out.println("body stream write error! " + e.getMessage());
                        completableFuture.completeExceptionally(e);
                    }
                    return this;
                }

                @Override
                public Body<HttpRest> flush() {
                    try {
                        request.getOutputStream().flush();
                    } catch (IOException e) {
                        System.out.println("body stream flush error! " + e.getMessage());
                        e.printStackTrace();
                        completableFuture.completeExceptionally(e);
                    }
                    return this;
                }

                @Override
                public HttpRest done() {
                    return HttpRest.this;
                }
            };
        }
        return body;
    }

    public final Future<HttpResponse> done() {
        try {
            willSendRequest();
            request.getOutputStream().close();
            request.getOutputStream().flush();
        } catch (Throwable e) {
            completableFuture.completeExceptionally(e);
        }
        try {
            return new Future<HttpResponse>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return completableFuture.cancel(mayInterruptIfRunning);
                }

                @Override
                public boolean isCancelled() {
                    return completableFuture.isCancelled();
                }

                @Override
                public boolean isDone() {
                    return completableFuture.isDone();
                }

                @Override
                public HttpResponse get() throws InterruptedException, ExecutionException {
                    return completableFuture.get();
                }

                @Override
                public HttpResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return completableFuture.get(timeout, unit);
                }

            };
        } finally {
            if (!HeaderValueEnum.CLOSE.getName().equals(request.getHeader(HeaderNameEnum.CONNECTION.getName()))) {
                semaphore.release();
            }
        }
    }

    public HttpRest onSuccess(Consumer<HttpResponse> consumer) {
        completableFuture.thenAccept(consumer);
        return this;
    }

    public HttpRest onFailure(Consumer<Throwable> consumer) {
        completableFuture.exceptionally(throwable -> {
            consumer.accept(throwable);
            return null;
        });
        return this;
    }

    public HttpRest setMethod(String method) {
        request.setMethod(method);
        return this;
    }


    public Header<? extends HttpRest> header() {
        return new Header<HttpRest>() {
            @Override
            public Header<HttpRest> add(String headerName, String headerValue) {
                request.addHeader(headerName, headerValue);
                return this;
            }

            @Override
            public Header<HttpRest> set(String headerName, String headerValue) {
                request.setHeader(headerName, headerValue);
                return this;
            }

            @Override
            public Header<HttpRest> setContentType(String contentType) {
                request.setContentType(contentType);
                return this;
            }

            @Override
            public Header<HttpRest> setContentLength(int contentLength) {
                request.setContentLength(contentLength);
                return this;
            }

            @Override
            public HttpRest done() {
                return HttpRest.this;
            }
        };
    }

    /**
     * 在 uri 后面添加请求参数
     *
     * @param name  参数名
     * @param value 参数值
     */
    public final HttpRest addQueryParam(String name, String value) {
        if (queryParams == null) {
            queryParams = new HashMap<>();
        }
        queryParams.put(name, value);
        return this;
    }

    /**
     * Http 响应事件
     */
    public HttpRest onResponse(ResponseHandler responseHandler) {
        this.responseHandler = Objects.requireNonNull(responseHandler);
        return this;
    }
}
