/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2021-02-08
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.ResponseHandler;
import org.smartboot.http.common.enums.DecodePartEnum;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.extension.processor.AbstractMessageProcessor;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor extends AbstractMessageProcessor<Response> {
    private final ExecutorService executorService;
    private final Map<AioSession, AbstractQueue<QueueUnit>> map = new ConcurrentHashMap<>();

    public HttpMessageProcessor() {
        this(null);
    }

    public HttpMessageProcessor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void process0(AioSession session, Response response) {
        ResponseAttachment responseAttachment = session.getAttachment();
        AbstractQueue<QueueUnit> queue = map.get(session);
        QueueUnit queueUnit = queue.peek();
        ResponseHandler responseHandler = queueUnit.getResponseHandler();

        switch (response.getDecodePartEnum()) {
            case HEADER_FINISH:
                doHttpHeader(response, responseHandler);
            case BODY:
                doHttpBody(response, session.readBuffer(), responseAttachment, responseHandler);
                if (response.getDecodePartEnum() != DecodePartEnum.FINISH) {
                    break;
                }
            case FINISH:
                queue.poll();
                responseAttachment.setDecoder(null);
                responseAttachment.setResponse(null);
                if (executorService == null) {
                    responseCallback(response, queueUnit);
                } else {
                    session.awaitRead();
                    executorService.execute(() -> {
                        responseCallback(response, queueUnit);
                        session.signalRead();
                    });
                }
                break;
            default:
        }
    }

    private void responseCallback(Response response, QueueUnit queueUnit) {
        queueUnit.getFuture().complete(response);
    }

    private void doHttpBody(Response response, ByteBuffer readBuffer, ResponseAttachment responseAttachment, ResponseHandler responseHandler) {
        if (responseHandler.onBodyStream(readBuffer, response)) {
            response.setDecodePartEnum(DecodePartEnum.FINISH);
        } else if (readBuffer.hasRemaining()) {
            //半包,继续读数据
            responseAttachment.setDecoder(HttpResponseProtocol.BODY_CONTINUE_DECODER);
        }
    }

    private void doHttpHeader(Response response, ResponseHandler responseHandler) {
        response.setDecodePartEnum(DecodePartEnum.BODY);
        try {
            responseHandler.onHeaderComplete(response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stateEvent0(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
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
                ResponseAttachment attachment = new ResponseAttachment();
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