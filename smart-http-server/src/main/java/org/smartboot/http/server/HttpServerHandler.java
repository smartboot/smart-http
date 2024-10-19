/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.ChunkedFrameDecoder;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.enums.HttpTypeEnum;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.io.BufferOutputStream;
import org.smartboot.http.common.io.ReadListener;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.impl.AbstractResponse;
import org.smartboot.http.server.impl.HttpMessageProcessor;
import org.smartboot.http.server.impl.HttpRequestImpl;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Http消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class HttpServerHandler implements ServerHandler<HttpRequest, HttpResponse> {

    @Override
    public void onBodyStream(ByteBuffer buffer, Request request) {
        HttpRequestImpl httpRequest = request.getRequestType() == HttpTypeEnum.HTTP_2 ? request.newHttp2Request() : request.newHttpRequest();
        if (HttpMethodEnum.GET.getMethod().equals(request.getMethod())) {
            handleHttpRequest(httpRequest);
            return;
        }
        long postLength = request.getContentLength();
        //Post请求
        if (HttpMethodEnum.POST.getMethod().equals(request.getMethod())
                && StringUtils.startsWith(request.getContentType(), HeaderValueEnum.X_WWW_FORM_URLENCODED.getName())
                && !HeaderValueEnum.UPGRADE.getName().equals(request.getConnection())) {
            if (postLength == 0) {
                handleHttpRequest(httpRequest);
                return;
            }

            SmartDecoder smartDecoder = httpRequest.getBodyDecoder();
            if (smartDecoder == null) {
                if (postLength > 0) {
                    smartDecoder = new FixedLengthFrameDecoder((int) postLength);
                } else if (HeaderValueEnum.CHUNKED.getName().equals(request.getHeader(HeaderNameEnum.TRANSFER_ENCODING.getName()))) {
                    smartDecoder = new ChunkedFrameDecoder();
                } else {
                    throw new HttpException(HttpStatus.LENGTH_REQUIRED);
                }
                httpRequest.setBodyDecoder(smartDecoder);
            }

            if (smartDecoder.decode(buffer)) {
                request.setFormUrlencoded(smartDecoder.getBuffer());
                httpRequest.setBodyDecoder(null);
                handleHttpRequest(httpRequest);
            }
        } else {
            handleHttpRequest(httpRequest);
        }
    }

    private void handleHttpRequest(HttpRequestImpl abstractRequest) {
        AbstractResponse response = abstractRequest.getResponse();
        CompletableFuture<Object> future = new CompletableFuture<>();
        boolean keepAlive = isKeepAlive(abstractRequest, response);
        abstractRequest.setKeepAlive(keepAlive);
        try {
            abstractRequest.request.getServerHandler().handle(abstractRequest, response, future);
            finishHttpHandle(abstractRequest, future);
        } catch (Throwable e) {
            HttpMessageProcessor.responseError(response, e);
        }
    }

    private void finishHttpHandle(HttpRequestImpl abstractRequest, CompletableFuture<Object> future) throws IOException {
        if (future.isDone()) {
            if (keepConnection(abstractRequest)) {
                finishResponse(abstractRequest);
            }
            return;
        }

        AioSession session = abstractRequest.request.getAioSession();
        ReadListener readListener = abstractRequest.getInputStream().getReadListener();
        if (readListener == null) {
            session.awaitRead();
        } else {
            //todo
//            abstractRequest.request.setDecoder(session.readBuffer().hasRemaining() ? HttpRequestProtocol.BODY_READY_DECODER : HttpRequestProtocol.BODY_CONTINUE_DECODER);
        }

        Thread thread = Thread.currentThread();
        AbstractResponse response = abstractRequest.getResponse();
        future.thenRun(() -> {
            try {
                if (keepConnection(abstractRequest)) {
                    finishResponse(abstractRequest);
                    if (thread != Thread.currentThread()) {
                        session.writeBuffer().flush();
                    }
                }
            } catch (Exception e) {
                HttpMessageProcessor.responseError(response, e);
            } finally {
                if (readListener == null) {
                    session.signalRead();
                }
            }
        }).exceptionally(throwable -> {
            try {
                HttpMessageProcessor.responseError(response, throwable);
            } finally {
                if (readListener == null) {
                    session.signalRead();
                }
            }
            return null;
        });
    }

    private void finishResponse(HttpRequestImpl abstractRequest) throws IOException {
        AbstractResponse response = abstractRequest.getResponse();
        //关闭本次请求的输出流
        BufferOutputStream bufferOutputStream = response.getOutputStream();
        if (!bufferOutputStream.isClosed()) {
            bufferOutputStream.close();
        }
        abstractRequest.reset();
    }

    private boolean keepConnection(HttpRequestImpl request) throws IOException {
        if (request.getResponse().isClosed()) {
            return false;
        }
        //非keepAlive或者 body部分未读取完毕,释放连接资源
        if (!request.isKeepAlive() || (!HttpMethodEnum.GET.getMethod().equals(request.getMethod()) && request.getContentLength() > 0 && request.getInputStream().available() > 0)) {
            request.getResponse().close();
            return false;
        }
        return true;
    }

    private boolean isKeepAlive(HttpRequestImpl abstractRequest, AbstractResponse response) {
        boolean keepAlive = !HeaderValueEnum.CLOSE.getName().equals(abstractRequest.getRequest().getConnection());
        // http/1.0默认短连接，http/1.1默认长连接。此处用 == 性能更高
        if (keepAlive && HttpProtocolEnum.HTTP_10.getProtocol() == abstractRequest.getProtocol()) {
            keepAlive = HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(abstractRequest.getHeader(HeaderNameEnum.CONNECTION.getName()));
            if (keepAlive) {
                response.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.KEEPALIVE.getName());
            }
        }
        return keepAlive;
    }

}
