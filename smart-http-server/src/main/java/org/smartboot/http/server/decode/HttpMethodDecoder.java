/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpTypeEnum;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public class HttpMethodDecoder extends AbstractDecoder {

    private final HttpUriDecoder decoder = new HttpUriDecoder(getConfiguration());

    public HttpMethodDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.SP);
        if (length > 0) {
            String method = StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length, StringUtils.String_CACHE_HTTP_METHOD);
            request.setMethod(method);
            if (method == HttpMethodEnum.CONNECT.getMethod()) {
                request.setType(HttpTypeEnum.PROXY_HTTPS);
            }
            return decoder.decode(byteBuffer, aioSession, request);
        } else {
            return this;
        }
    }
}
