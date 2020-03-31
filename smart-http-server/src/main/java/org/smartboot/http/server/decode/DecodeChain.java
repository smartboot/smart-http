/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: DecodeChain.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.server.BaseHttpRequest;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public interface DecodeChain {

    public DecodeChain deocde(ByteBuffer byteBuffer, char[] cacheChars, AioSession<BaseHttpRequest> aioSession, BaseHttpRequest httpHeader);

}
