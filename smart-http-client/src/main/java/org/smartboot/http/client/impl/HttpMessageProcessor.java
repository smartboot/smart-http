/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2021-02-08
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.HttpResponse;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor implements MessageProcessor<Response> {
    private final ExecutorService executorService;
    private final Map<AioSession, AbstractQueue<CompletableFuture<HttpResponse>>> map = new ConcurrentHashMap<>();

    public HttpMessageProcessor() {
        this(null);
    }

    public HttpMessageProcessor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void process(AioSession session, Response baseHttpResponse) {
        CompletableFuture<HttpResponse> httpRest = map.get(session).poll();
        if (executorService == null) {
            httpRest.complete(baseHttpResponse);
        } else {
            session.awaitRead();
            executorService.execute(() -> {
                httpRest.complete(baseHttpResponse);
                session.signalRead();
            });
        }
    }

    @Override
    public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        if (throwable != null) {
            AbstractQueue<CompletableFuture<HttpResponse>> queue = map.get(session);
            if (queue != null) {
                CompletableFuture<HttpResponse> future;
                while ((future = queue.poll()) != null) {
                    future.completeExceptionally(throwable);
                }
            }
        }
        switch (stateMachineEnum) {
            case NEW_SESSION:
                map.put(session, new ConcurrentLinkedQueue<>());
                ResponseAttachment attachment = new ResponseAttachment(new Response());
                session.setAttachment(attachment);
                break;
            case PROCESS_EXCEPTION:
                session.close();
                break;
            case DECODE_EXCEPTION:
                throwable.printStackTrace();
                break;
            case SESSION_CLOSED:
                AbstractQueue<CompletableFuture<HttpResponse>> queue = map.remove(session);
                CompletableFuture<HttpResponse> future;
                while ((future = queue.poll()) != null) {
                    future.completeExceptionally(new IOException("client is closed"));
                }
                break;
        }
    }

    public AbstractQueue<CompletableFuture<HttpResponse>> getQueue(AioSession aioSession) {
        return map.get(aioSession);
    }
}