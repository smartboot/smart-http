/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.RequestAttachment;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpHeaderEndDecoder implements Decoder {

    private final HttpBodyDecoder decoder = new HttpBodyDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request request) {
        if (HttpMethodEnum.GET.getMethod().equals(request.getMethod())) {
            if (request.isWebsocket()) {
                WebSocketRequestImpl webSocketRequest = new WebSocketRequestImpl(request);
                RequestAttachment attachment = aioSession.getAttachment();
                attachment.setWebSocketRequest(webSocketRequest);
                return HttpRequestProtocol.WS_HANDSHAKE_DECODER;
            } else {
                return HttpRequestProtocol.HTTP_FINISH_DECODER;
            }
        }
        //Post请求
        if (HttpMethodEnum.POST.getMethod().equals(request.getMethod())
                && StringUtils.startsWith(request.getContentType(), HeaderValueEnum.X_WWW_FORM_URLENCODED.getName())) {
            int postLength = request.getContentLength();
            if (postLength > Constant.maxPostSize) {
                throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
            } else if (postLength < 0) {
                throw new HttpException(HttpStatus.LENGTH_REQUIRED);
            }
            return decoder.decode(byteBuffer, aioSession, request);
        } else {
            return HttpRequestProtocol.HTTP_FINISH_DECODER;
        }
    }
}
