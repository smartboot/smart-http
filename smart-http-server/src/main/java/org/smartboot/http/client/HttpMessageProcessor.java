/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpMessageProcessor.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.WebSocketRequest;
import org.smartboot.http.WebSocketResponse;
import org.smartboot.http.common.HandlePipeline;
import org.smartboot.http.common.HttpClientHandle;
import org.smartboot.http.common.WebSocketHandle;
import org.smartboot.http.enums.YesNoEnum;
import org.smartboot.http.logging.Logger;
import org.smartboot.http.logging.LoggerFactory;
import org.smartboot.http.server.handle.Pipeline;
import org.smartboot.http.utils.AttachKey;
import org.smartboot.http.utils.Attachment;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor implements MessageProcessor<Response> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessageProcessor.class);
    /**
     * HttpRequest附件Key
     */
    private final AttachKey<HttpResponseImpl> ATTACH_KEY_HTTP_RESPONSE = AttachKey.valueOf("httpRequest");
    /**
     * Http消息处理管道
     */
    private final HandlePipeline<HttpRequest, HttpResponse> httpPipeline = new HandlePipeline<>();
    /**
     * Websocket处理管道
     */
    private final HandlePipeline<WebSocketRequest, WebSocketResponse> wsPipeline = new HandlePipeline<>();

    public HttpMessageProcessor() {
//        httpPipeline.next(new HttpExceptionHandle()).next(new RFC2612RequestHandle());
//        wsPipeline.next(new WebSocketHandSharkHandle());
    }

    @Override
    public void process(AioSession session, Response baseHttpResponse) {
        Attachment attachment = session.getAttachment();
        AbstractResponse response;
        AbstractRequest request;
        HandlePipeline pipeline;
        if (baseHttpResponse.isWebsocket() == YesNoEnum.Yes) {
            WebSocketResponseImpl webSocketRequest = attachment.get(HttpResponseProtocol.ATTACH_KEY_WS_REQ);
            response = webSocketRequest;
//                request = webSocketRequest.getResponse();
            pipeline = wsPipeline;
        } else {
            HttpResponseImpl httpResponse = attachment.get(ATTACH_KEY_HTTP_RESPONSE);
            if (httpResponse == null) {
                httpResponse = new HttpResponseImpl(baseHttpResponse);
                attachment.put(ATTACH_KEY_HTTP_RESPONSE, httpResponse);
            }
//                response = http11Request;
//                request = http11Request.getResponse();
            pipeline = httpPipeline;
        }

        //消息处理
//            pipeline.doHandle(response, request);

        //关闭本次请求的输出流
//            if (!request.getOutputStream().isClosed()) {
//                request.getOutputStream().close();
//            }

        //response被closed,则断开TCP连接
//            if (request.isClosed()) {
//                session.close(false);
//            } else {
//                //复用长连接
//                response.reset();
//            }
    }

    @Override
    public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                Attachment attachment = new Attachment();
                attachment.put(HttpResponseProtocol.ATTACH_KEY_RESPONSE, new Response(session));
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

    public Pipeline<HttpRequest, HttpResponse> pipeline(HttpClientHandle httpHandle) {
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
