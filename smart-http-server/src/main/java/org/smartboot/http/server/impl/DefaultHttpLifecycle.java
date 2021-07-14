/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: DefaultHttpLifecycle.java
 * Date: 2021-07-14
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpLifecycle;
import org.smartboot.http.server.HttpResponse;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/13
 */
public class DefaultHttpLifecycle implements HttpLifecycle {
    SmartDecoder smartDecoder;
    

    @Override
    public boolean decode(ByteBuffer buffer, Request request, HttpResponse response) {
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
            if (smartDecoder == null) {
                smartDecoder = new FixedLengthFrameDecoder(request.getContentLength());
            }

            if (smartDecoder.decode(buffer)) {
                request.setFormUrlencoded(new String(smartDecoder.getBuffer().array()));
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
