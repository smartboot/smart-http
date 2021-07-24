/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.Handler;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Http消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class HttpServerHandler extends Handler<HttpRequest, HttpResponse, Request> {
    private final Map<Request, SmartDecoder> bodyDecoderMap = new ConcurrentHashMap<>();

    @Override
    public boolean onBodyStream(ByteBuffer buffer, Request request) {
        if (HttpMethodEnum.GET.getMethod().equals(request.getMethod())) {
            return true;
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
            SmartDecoder smartDecoder = bodyDecoderMap.computeIfAbsent(request, req -> new FixedLengthFrameDecoder(req.getContentLength()));

            if (smartDecoder.decode(buffer)) {
                bodyDecoderMap.remove(request);
                request.setFormUrlencoded(new String(smartDecoder.getBuffer().array()));
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }


    @Override
    public void onClose(Request request) {
        bodyDecoderMap.remove(request);
    }

}
