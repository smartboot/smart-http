/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HeaderDecoder.java
 * Date: 2021-07-15
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.impl.Response;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * Http Header解码器
 *
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public interface HeaderDecoder {

    HeaderDecoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response);

}
