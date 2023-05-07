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
class HttpProtocolDecoder extends AbstractDecoder {

    private final HttpHeaderDecoder decoder = new HttpHeaderDecoder(getConfiguration());

    private final LfDecoder lfDecoder = new LfDecoder(decoder, getConfiguration());

    public HttpProtocolDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

//    @Override
//    public Decoder decode(ByteBuffer byteBuffer, Request request) {
//        String protocol = HttpUtils.getString(byteBuffer, CR_END_MATCHER);
//        if (protocol != null) {
//            request.setProtocol(protocol);
//            return lfDecoder.decode(byteBuffer, request);
//        } else {
//            return this;
//        }
//
//    }

    @Override
    public Decoder decode(ByteBuffer byteBuffer, Request request) {
        ByteTree<?> protocol = StringUtils.scanByteTree(byteBuffer, CR_END_MATCHER, getConfiguration().getByteCache());
        if (protocol != null) {
            request.setProtocol(protocol.getStringValue());
            return lfDecoder.decode(byteBuffer, request);
        } else {
            return this;
        }

    }
}
