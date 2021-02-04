/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.impl.HttpResponseProtocol;
import org.smartboot.http.client.impl.Response;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpHeaderEndDecoder implements Decoder {

    private final HttpBodyDecoder decoder = new HttpBodyDecoder();
    private final HttpChunkedBodyDecoder chunkedBodyDecoder = new HttpChunkedBodyDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
        //识别是否websocket通信
//        if (response.isWebsocket() == null) {
//            response.setWebsocket(HttpHeaderConstant.Values.WEBSOCKET.equals(response.getHeader(HttpHeaderConstant.Names.UPGRADE))
//                    && HttpHeaderConstant.Values.UPGRADE.equals(response.getHeader(HttpHeaderConstant.Names.CONNECTION)) ? YesNoEnum.Yes : YesNoEnum.NO);
//        }
//        if (HttpMethodEnum.GET.getMethod().equals(response.getMethod())) {
//            if (response.isWebsocket() == YesNoEnum.Yes) {
//                WebSocketResponseImpl webSocketRequest = new WebSocketResponseImpl(response);
//                Attachment attachment = aioSession.getAttachment();
//                attachment.put(ATTACH_KEY_WS_REQ, webSocketRequest);
//                return HttpResponseProtocol.WS_HANDSHARK_DECODER;
//            } else {
//                return HttpResponseProtocol.HTTP_FINISH_DECODER;
//            }
//        }
        //
        String transferEncoding = response.getHeader(HttpHeaderConstant.Names.TRANSFER_ENCODING);
        if (StringUtils.equals(transferEncoding, HttpHeaderConstant.Values.CHUNKED)) {
            return chunkedBodyDecoder.decode(byteBuffer, aioSession, response);
        }
        int bodyLength = response.getContentLength();
        if (bodyLength > Constant.maxPostSize) {
            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        return bodyLength > 0 ? decoder.decode(byteBuffer, aioSession, response) : HttpResponseProtocol.HTTP_FINISH_DECODER;
    }
}
