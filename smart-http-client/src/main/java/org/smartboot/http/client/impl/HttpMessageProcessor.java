/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2021-02-08
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.common.enums.DecodePartEnum;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor implements MessageProcessor<Response> {
    private final ExecutorService executorService;
    private final Map<AioSession, AbstractQueue<QueueUnit>> map = new ConcurrentHashMap<>();

    public HttpMessageProcessor() {
        this(null);
    }

    public HttpMessageProcessor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void process(AioSession session, Response response) {
        ResponseAttachment responseAttachment = session.getAttachment();
        AbstractQueue<QueueUnit> queue = map.get(session);
        QueueUnit queueUnit = queue.peek();
        //Http Header解析成功
        if (response.getDecodePartEnum() == DecodePartEnum.HEADER_FINISH) {
            response.setDecodePartEnum(DecodePartEnum.BODY);
            try {
                queueUnit.getResponseHandler().onHeaderComplete(response);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        //定义 body 解码
        if (response.getDecodePartEnum() == DecodePartEnum.BODY) {
            if (queueUnit.getResponseHandler().onBodyStream(responseAttachment.getByteBuffer(), response)) {
                response.setDecodePartEnum(DecodePartEnum.FINISH);
            }
        }
        //解码完成
        if (response.getDecodePartEnum() == DecodePartEnum.FINISH) {
            queue.poll();
            if (executorService == null) {
                queueUnit.getFuture().complete(response);
            } else {
                session.awaitRead();
                executorService.execute(() -> {
                    queueUnit.getFuture().complete(response);
                    session.signalRead();
                });
            }
        }
    }

    @Override
    public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        if (throwable != null) {
            AbstractQueue<QueueUnit> queue = map.get(session);
            if (queue != null) {
                QueueUnit future;
                while ((future = queue.poll()) != null) {
                    future.getFuture().completeExceptionally(throwable);
                }
            }
        }
        switch (stateMachineEnum) {
            case NEW_SESSION:
                map.put(session, new ConcurrentLinkedQueue<>());
                ResponseAttachment attachment = new ResponseAttachment(new Response(session));
                session.setAttachment(attachment);
                break;
            case PROCESS_EXCEPTION:
                session.close();
                break;
            case DECODE_EXCEPTION:
                throwable.printStackTrace();
                break;
            case SESSION_CLOSED:
                AbstractQueue<QueueUnit> queue = map.remove(session);
                QueueUnit future;
                while ((future = queue.poll()) != null) {
                    future.getFuture().completeExceptionally(new IOException("client is closed"));
                }
                break;
        }
    }

    public AbstractQueue<QueueUnit> getQueue(AioSession aioSession) {
        return map.get(aioSession);
    }
}