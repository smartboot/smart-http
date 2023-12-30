/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2021-02-08
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.decode.HeaderDecoder;
import org.smartboot.http.common.enums.DecodePartEnum;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.extension.processor.AbstractMessageProcessor;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
final class HttpMessageProcessor extends AbstractMessageProcessor<AbstractResponse> {
    private final ExecutorService executorService;

    public HttpMessageProcessor() {
        this(null);
    }

    public HttpMessageProcessor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void process0(AioSession session, AbstractResponse response) {
        ResponseAttachment responseAttachment = session.getAttachment();
        ResponseHandler responseHandler = response.getResponseHandler();

        switch (response.getDecodePartEnum()) {
            case HEADER_FINISH:
                doHttpHeader(response, responseHandler);
            case BODY:
                doHttpBody(response, session.readBuffer(), responseAttachment, responseHandler);
                if (response.getDecodePartEnum() != DecodePartEnum.FINISH) {
                    break;
                }
            case FINISH:
                if (executorService == null) {
                    response.getFuture().complete(response);
                } else {
                    session.awaitRead();
                    executorService.execute(() -> {
                        response.getFuture().complete(response);
                        session.signalRead();
                    });
                }
                break;
            default:
        }
    }

    private void doHttpBody(AbstractResponse response, ByteBuffer readBuffer, ResponseAttachment responseAttachment, ResponseHandler responseHandler) {
        if (responseHandler.onBodyStream(readBuffer, response)) {
            response.setDecodePartEnum(DecodePartEnum.FINISH);
        } else if (readBuffer.hasRemaining()) {
            //半包,继续读数据
            responseAttachment.setDecoder(HeaderDecoder.BODY_CONTINUE_DECODER);
        }
    }

    private void doHttpHeader(AbstractResponse response, ResponseHandler responseHandler) {
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

        switch (stateMachineEnum) {
            case NEW_SESSION:
                ResponseAttachment attachment = new ResponseAttachment();
                session.setAttachment(attachment);
                break;
            case PROCESS_EXCEPTION:
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                session.close();
                break;
            case DECODE_EXCEPTION:
                throwable.printStackTrace();
                break;
            case SESSION_CLOSED:
                break;
        }
    }

}