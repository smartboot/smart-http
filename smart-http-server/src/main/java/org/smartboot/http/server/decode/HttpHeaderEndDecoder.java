/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.BasicHttpRequest;
import org.smartboot.http.server.HttpRequestProtocol;
import org.smartboot.http.server.WebSocketRequestImpl;
import org.smartboot.http.utils.Attachment;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

import static org.smartboot.http.server.HttpRequestProtocol.ATTACH_KEY_WS_REQ;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpHeaderEndDecoder implements Decoder {

    private final HttpBodyDecoder decoder = new HttpBodyDecoder();

    @Override
    public Decoder deocde(ByteBuffer byteBuffer, char[] cacheChars, AioSession<BasicHttpRequest> aioSession, BasicHttpRequest request) {
        //websocket通信
        if (HttpMethodEnum.GET.getMethod().equals(request.getMethod())
                && HttpHeaderConstant.Values.WEBSOCKET.equals(request.getHeader(HttpHeaderConstant.Names.UPGRADE))
                && HttpHeaderConstant.Values.UPGRADE.equals(request.getHeader(HttpHeaderConstant.Names.CONNECTION))) {
            request.setWebsocket(true);
            WebSocketRequestImpl webSocketRequest = new WebSocketRequestImpl(request);
            Attachment attachment = aioSession.getAttachment();
            attachment.put(ATTACH_KEY_WS_REQ, webSocketRequest);
            return HttpRequestProtocol.WS_HANDSHARK_DECODER;
        }
        //Post请求
        else if (HttpMethodEnum.POST.getMethod().equals(request.getMethod())
                && StringUtils.startsWith(request.getContentType(), HttpHeaderConstant.Values.X_WWW_FORM_URLENCODED)) {
            int postLength = request.getContentLength();
            if (postLength > Constant.maxPostSize) {
                throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
            } else if (postLength <= 0) {
                throw new HttpException(HttpStatus.LENGTH_REQUIRED);
            }
            return decoder.deocde(byteBuffer, cacheChars, aioSession, request);
        } else {
            return HttpRequestProtocol.HTTP_FINISH_DECODER;
        }
    }
}
