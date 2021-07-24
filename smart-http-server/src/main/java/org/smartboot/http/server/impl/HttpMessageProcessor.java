/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.Handler;
import org.smartboot.http.common.HandlerPipeline;
import org.smartboot.http.common.Pipeline;
import org.smartboot.http.common.enums.DecodePartEnum;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor implements MessageProcessor<Request> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessageProcessor.class);
    /**
     * Http消息处理管道
     */
    protected final HandlerPipeline<HttpRequest, HttpResponse, Request> httpPipeline = new HandlerPipeline<>();
    /**
     * Websocket处理管道
     */
    protected final HandlerPipeline<WebSocketRequest, WebSocketResponse, Request> wsPipeline = new HandlerPipeline<>();

    public HttpMessageProcessor() {
        httpPipeline.next(new BasicHttpServerHandler());
    }

    @Override
    public void process(AioSession session, Request request) {
        RequestAttachment attachment = session.getAttachment();
        AbstractRequest abstractRequest = null;
        HandlerPipeline pipeline = null;

        if (request.isWebsocket()) {
            abstractRequest = request.newWebsocketRequest();
            pipeline = wsPipeline;
        } else {
            abstractRequest = request.newHttpRequest();
            pipeline = httpPipeline;
        }

        //Header解码成功
        if (request.getDecodePartEnum() == DecodePartEnum.HEADER_FINISH) {
            try {
                pipeline.onHeaderComplete(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            request.setDecodePartEnum(DecodePartEnum.BODY);
            if (abstractRequest.getResponse().isClosed()) {
                session.close(false);
                return;
            }
        }
        //解码 Body 内容
        if (request.getDecodePartEnum() == DecodePartEnum.BODY) {
            if (pipeline.onBodyStream(attachment.getReadBuffer(), request) == Handler.BODY_FINISH) {
                request.setDecodePartEnum(DecodePartEnum.FINISH);
            }
        }
        if (request.getDecodePartEnum() == DecodePartEnum.FINISH) {
            AbstractResponse response = abstractRequest.getResponse();
            try {
                if (!response.isClosed()) {
                    //消息处理
                    pipeline.handle(abstractRequest, response);
                    //关闭本次请求的输出流
                    if (!response.getOutputStream().isClosed()) {
                        response.getOutputStream().close();
                    }
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            } finally {
                //response被closed,则断开TCP连接
                if (response.isClosed()) {
                    session.close(false);
                } else {
                    //复用长连接
                    request.reset();
                    attachment.setDecoder(null);
                }
            }
        }
    }

    @Override
    public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                RequestAttachment attachment = new RequestAttachment(new Request(session));
                session.setAttachment(attachment);
                break;
            case PROCESS_EXCEPTION:
                LOGGER.error("process exception", throwable);
                session.close();
                break;
            case SESSION_CLOSED:
                RequestAttachment att = session.getAttachment();
                if (att.getRequest().isWebsocket()) {
                    wsPipeline.onClose(att.getRequest());
                } else {
                    httpPipeline.onClose(att.getRequest());
                }
                break;
            case DECODE_EXCEPTION:
                throwable.printStackTrace();
                break;
        }
    }

    public Pipeline<HttpRequest, HttpResponse, Request> pipeline(HttpServerHandler httpHandle) {
        return httpPipeline.next(httpHandle);
    }

    public Pipeline<HttpRequest, HttpResponse, Request> pipeline() {
        return httpPipeline;
    }

    public Pipeline<WebSocketRequest, WebSocketResponse, Request> wsPipeline(WebSocketHandler httpHandle) {
        return wsPipeline.next(httpHandle);
    }

    public Pipeline<WebSocketRequest, WebSocketResponse, Request> wsPipeline() {
        return wsPipeline;
    }

}
