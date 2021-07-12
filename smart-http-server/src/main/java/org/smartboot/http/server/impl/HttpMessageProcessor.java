/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.HandlerPipeline;
import org.smartboot.http.common.Pipeline;
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
    protected final HandlerPipeline<HttpRequest, HttpResponse> httpPipeline = new HandlerPipeline<>();
    /**
     * Websocket处理管道
     */
    protected final HandlerPipeline<WebSocketRequest, WebSocketResponse> wsPipeline = new HandlerPipeline<>();

    public HttpMessageProcessor() {
        httpPipeline.next(new HttpExceptionHandler()).next(new RFC2612RequestHandler());
        wsPipeline.next(new WebSocketHandSharkHandler());
    }

    @Override
    public void process(AioSession session, Request baseHttpRequest) {
        try {
            switch (baseHttpRequest.getType()) {
                case PROXY_HTTPS:
                    throw new UnsupportedOperationException();
                case WEBSOCKET:
                    websocketHandle(session);
                    break;
                default:
                    httpHandle(session, baseHttpRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理 Http 消息
     *
     * @param session
     * @param request
     * @throws IOException
     */
    private void httpHandle(AioSession session, Request request) throws IOException {
        HttpRequestImpl httpRequest = new HttpRequestImpl(request);
        HttpResponseImpl response = httpRequest.getResponse();
        //消息处理
        httpPipeline.handle(httpRequest, response);

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
    }

    private void websocketHandle(AioSession session) throws IOException {
        RequestAttachment attachment = session.getAttachment();

        WebSocketRequestImpl webSocketRequest = attachment.getWebSocketRequest();
        WebSocketResponseImpl response = webSocketRequest.getResponse();

        //消息处理
        wsPipeline.handle(webSocketRequest, webSocketRequest.getResponse());

        //关闭本次请求的输出流
        if (!response.getOutputStream().isClosed()) {
            response.getOutputStream().close();
        }

        //response被closed,则断开TCP连接
        if (response.isClosed()) {
            session.close(false);
        } else {
            //复用长连接
            webSocketRequest.reset();
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

    public Pipeline<HttpRequest, HttpResponse> pipeline(HttpServerHandler httpHandle) {
        return httpPipeline.next(httpHandle);
    }

    public Pipeline<HttpRequest, HttpResponse> pipeline() {
        return httpPipeline;
    }

    public Pipeline<WebSocketRequest, WebSocketResponse> wsPipeline(WebSocketHandler httpHandle) {
        return wsPipeline.next(httpHandle);
    }

    public Pipeline<WebSocketRequest, WebSocketResponse> wsPipeline() {
        return wsPipeline;
    }

}
