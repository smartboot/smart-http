/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequestImpl.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.socket.transport.WriteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
public final class HttpRequestImpl extends AbstractRequest {

    public HttpRequestImpl(WriteBuffer outputStream) {
        init(new HttpOutputStream(this, outputStream));
    }
}
