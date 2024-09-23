/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpStatusDescDecoder.java
 * Date: 2021-07-15
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.common.utils.ByteTree;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpStatusDescDecoder implements HeaderDecoder {

    private final HttpHeaderDecoder decoder = new HttpHeaderDecoder();
    private final LfDecoder lfDecoder = new LfDecoder(decoder);

    @Override
    public HeaderDecoder decode(ByteBuffer byteBuffer, AioSession aioSession, AbstractResponse request) {
        ByteTree<?> byteTree = StringUtils.scanByteTree(byteBuffer, ByteTree.CR_END_MATCHER, ByteTree.DEFAULT);
        if (byteTree != null) {
            request.setReasonPhrase(byteTree.getStringValue());
            return lfDecoder.decode(byteBuffer, aioSession, request);
        } else {
            return this;
        }

    }
}
