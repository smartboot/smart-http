/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: FinishDecoder.java
 * Date: 2020-03-31
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.server.Http11Request;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/31
 */
public class FinishDecoder implements DecodeChain<Http11Request> {
    @Override
    public DecodeChain<Http11Request> deocde(ByteBuffer byteBuffer, char[] cacheChars, AioSession<Http11Request> aioSession, Http11Request requestBuilder) {
        return null;
    }
}
