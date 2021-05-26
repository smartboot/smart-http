/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.HandlePipeline;
import org.smartboot.http.common.Pipeline;
import org.smartboot.http.common.enums.YesNoEnum;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.WebSocketHandle;
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
    private final HandlePipeline<HttpRequest, HttpResponse> httpPipeline = new HandlePipeline<>();
    /**
     * Websocket处理管道
     */
    private final HandlePipeline<WebSocketRequest, WebSocketResponse> wsPipeline = new HandlePipeline<>();

    public HttpMessageProcessor() {
        httpPipeline.next(new HttpExceptionHandle()).next(new RFC2612RequestHandle());
        wsPipeline.next(new WebSocketHandSharkHandle());
    }

    @Override
    public void process(AioSession session, Request baseHttpRequest) {
        try {
            RequestAttachment attachment = session.getAttachment();
            AbstractRequest request;
            AbstractResponse response;
            HandlePipeline pipeline;
            if (baseHttpRequest.isWebsocket() == YesNoEnum.Yes) {
                request = attachment.getWebSocketRequest();
                response = attachment.getWebSocketRequest().getResponse();
                pipeline = wsPipeline;
            } else {
                HttpRequestImpl http11Request = attachment.getHttpRequest();
                if (http11Request == null) {
                    http11Request = new HttpRequestImpl(baseHttpRequest);
                    attachment.setHttpRequest(http11Request);
                }
                request = http11Request;
                response = http11Request.getResponse();
                pipeline = httpPipeline;
            }

            //消息处理
            pipeline.doHandle(request, response);

            //关闭本次请求的输出流
            if (!response.getOutputStream().isClosed()) {
                response.getOutputStream().close();
            }

            //response被closed,则断开TCP连接
            if (response.isClosed()) {
                session.close(false);
            } else {
                //复用长连接
                request.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
//            case INPUT_SHUTDOWN:
//                LOGGER.error("inputShutdown", throwable);
//                break;
//            case OUTPUT_EXCEPTION:
//                LOGGER.error("", throwable);
//                break;
//            case INPUT_EXCEPTION:
//                LOGGER.error("",throwable);
//                break;
//            case SESSION_CLOSED:
//                System.out.println("closeSession");
//                LOGGER.info("connection closed:{}");
//                break;
            case DECODE_EXCEPTION:
                throwable.printStackTrace();
                break;
//                default:
//                    System.out.println(stateMachineEnum);
        }
    }

    public Pipeline<HttpRequest, HttpResponse> pipeline(HttpServerHandle httpHandle) {
        return httpPipeline.next(httpHandle);
    }

    public Pipeline<HttpRequest, HttpResponse> pipeline() {
        return httpPipeline;
    }

    public Pipeline<WebSocketRequest, WebSocketResponse> wsPipeline(WebSocketHandle httpHandle) {
        return wsPipeline.next(httpHandle);
    }

    public Pipeline<WebSocketRequest, WebSocketResponse> wsPipeline() {
        return wsPipeline;
    }

}
