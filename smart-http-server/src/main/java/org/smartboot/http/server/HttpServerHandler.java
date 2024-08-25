/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.ChunkedFrameDecoder;
import org.smartboot.http.common.enums.BodyStreamStatus;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.decode.MultipartFormDecoder;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

/**
 * Http消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class HttpServerHandler implements ServerHandler<HttpRequest, HttpResponse> {

    @Override
    public BodyStreamStatus onBodyStream(ByteBuffer buffer, Request request) {
        if (HttpMethodEnum.GET.getMethod().equals(request.getMethod())) {
            return BodyStreamStatus.Finish;
        }
        long postLength = request.getContentLength();
        //Post请求
        if (HttpMethodEnum.POST.getMethod().equals(request.getMethod())
                && StringUtils.startsWith(request.getContentType(), HeaderValueEnum.X_WWW_FORM_URLENCODED.getName())
                && !HeaderValueEnum.UPGRADE.getName().equals(request.getConnection())) {
            if (postLength == 0) {
                return BodyStreamStatus.Finish;
            }

            SmartDecoder smartDecoder = request.getBodyDecoder();
            if (smartDecoder == null) {
                if (postLength > 0) {
                    smartDecoder = new FixedLengthFrameDecoder((int) postLength);
                } else if (HeaderValueEnum.CHUNKED.getName().equals(request.getHeader(HeaderNameEnum.TRANSFER_ENCODING.getName()))) {
                    smartDecoder = new ChunkedFrameDecoder();
                } else {
                    throw new HttpException(HttpStatus.LENGTH_REQUIRED);
                }
                request.setBodyDecoder(smartDecoder);
            }

            if (smartDecoder.decode(buffer)) {
                request.setFormUrlencoded(smartDecoder.getBuffer());
                request.setBodyDecoder(null);
                return BodyStreamStatus.Finish;
            } else {
                return BodyStreamStatus.Continue;
            }
        } else if (HttpMethodEnum.POST.getMethod().equals(request.getMethod())
                && StringUtils.startsWith(request.getContentType(), HeaderValueEnum.MULTIPART_FORM_DATA.getName())
                && !HeaderValueEnum.UPGRADE.getName().equals(request.getConnection())) {
            if (postLength < 0) {
                throw new HttpException(HttpStatus.LENGTH_REQUIRED);
            } else if (postLength == 0 || request.isMultipartParsed()) {
                return BodyStreamStatus.Finish;
            } else if (buffer.position() == buffer.limit()) {
                return BodyStreamStatus.Continue;
            }
            Decoder decoder = new MultipartFormDecoder(request).decode(buffer, request);
            if (decoder != HttpRequestProtocol.BODY_READY_DECODER) {
                request.setDecoder(decoder);
                return BodyStreamStatus.OnAsync;
            } else {
                return BodyStreamStatus.Finish;
            }
        } else {
            return BodyStreamStatus.Finish;
        }
    }

}
