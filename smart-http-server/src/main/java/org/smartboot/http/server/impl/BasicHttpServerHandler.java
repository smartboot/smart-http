/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpExceptionHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Http异常统一处理
 *
 * @author 三刀
 * @version V1.0 , 2020/6/23
 */
class BasicHttpServerHandler extends HttpServerHandler {
    private final HttpServerHandler nextHandler;

    public BasicHttpServerHandler(HttpServerHandler httpServerHandler) {
        this.nextHandler = httpServerHandler;
    }

    @Override
    public boolean onBodyStream(ByteBuffer buffer, Request request) {
        return nextHandler.onBodyStream(buffer, request);
    }

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        nextHandler.onHeaderComplete(request);
    }

    @Override
    public void onClose(Request request) {
        nextHandler.onClose(request);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        try {
            boolean keepAlive = true;
            // http/1.0兼容长连接。此处用 == 性能更高
            if (HttpProtocolEnum.HTTP_10.getProtocol() == request.getProtocol()) {
                keepAlive = HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(request.getHeader(HeaderNameEnum.CONNECTION.getName()));
                if (keepAlive) {
                    response.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.KEEPALIVE.getName());
                }
            }

            nextHandler.handle(request, response);

            if (!keepAlive) {
                response.close();
            }
            //body部分未读取完毕,释放连接资源
            if (!HttpMethodEnum.GET.getMethod().equals(request.getMethod())
                    && request.getContentLength() > 0
                    && request.getInputStream().available() > 0) {
                response.close();
            }
        } catch (HttpException e) {
            e.printStackTrace();
            response.setHttpStatus(HttpStatus.valueOf(e.getHttpCode()));
            response.getOutputStream().write(e.getDesc().getBytes());
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getOutputStream().write(e.fillInStackTrace().toString().getBytes());
            response.close();
        }
    }
}
