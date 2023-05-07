/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.utils.ByteTree;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.impl.Request;

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

//    @Override
//    public Decoder decode(ByteBuffer byteBuffer, Request request) {
//        String method = HttpUtils.getString(byteBuffer, SP_END_MATCHER);
//        if (method != null) {
//            request.setMethod(method);
//            return decoder.decode(byteBuffer, request);
//        } else {
//            return this;
//        }
//    }

    @Override
    public Decoder decode(ByteBuffer byteBuffer, Request request) {
        ByteTree<?> method = StringUtils.scanByteTree(byteBuffer, SP_END_MATCHER, getConfiguration().getByteCache());
        if (method != null) {
            request.setMethod(method.getStringValue());
            return decoder.decode(byteBuffer, request);
        } else {
            return this;
        }
    }
}
