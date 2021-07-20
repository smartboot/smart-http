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
import org.smartboot.http.common.enums.DecodePartEnum;
import org.smartboot.http.common.enums.HttpTypeEnum;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.server.HttpLifecycle;
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
import java.util.Objects;
import java.util.function.Function;

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

    private Function<Request, HttpLifecycle> httpLifecycleFunction = request -> new DefaultHttpLifecycle();

    public HttpMessageProcessor() {
        httpPipeline.next(new HttpExceptionHandler()).next(new RFC2612RequestHandler());
        wsPipeline.next(new WebSocketHandSharkHandler());
    }

    public void setHttpLifecycleFunction(Function<Request, HttpLifecycle> httpLifecycleFunction) {
        this.httpLifecycleFunction = Objects.requireNonNull(httpLifecycleFunction);
    }

    @Override
    public void process(AioSession session, Request request) {
        RequestAttachment attachment = session.getAttachment();
        AbstractRequest abstractRequest = null;
        HandlerPipeline pipeline = null;
        if (request.getType() == HttpTypeEnum.WEBSOCKET) {
            abstractRequest = request.newWebsocketRequest();
            pipeline = wsPipeline;
        } else {
            abstractRequest = request.newHttpRequest();
            pipeline = httpPipeline;
        }
        //定义 body 解码器
        if (request.getDecodePartEnum() == DecodePartEnum.HEADER_FINISH) {

            request.setHttpLifecycle(httpLifecycleFunction.apply(request));
            request.setDecodePartEnum(DecodePartEnum.BODY);

            request.getHttpLifecycle().onHeaderComplete(request);
            if (abstractRequest.getResponse().isClosed()) {
                session.close(false);
                return;
            }
        }
        //定义 body 解码
        if (request.getDecodePartEnum() == DecodePartEnum.BODY) {
            if (request.getHttpLifecycle().onBodyStream(attachment.getReadBuffer(), request)) {
                request.setDecodePartEnum(DecodePartEnum.FINISH);
            }
        }
        if (request.getDecodePartEnum() == DecodePartEnum.FINISH) {
            AbstractResponse response = abstractRequest.getResponse();
            try {
                //消息处理
                pipeline.handle(abstractRequest, abstractRequest.getResponse());
                //关闭本次请求的输出流
                if (!response.getOutputStream().isClosed()) {
                    response.getOutputStream().close();
                }

            } catch (IOException e) {
                e.printStackTrace();
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
                att.getRequest().getHttpLifecycle().onClose();
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
