/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpResponseImpl.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.socket.transport.WriteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
class HttpResponseImpl extends AbstractResponse {

    public HttpResponseImpl(HttpRequestImpl request, WriteBuffer outputStream) {
        init(request, new HttpOutputStream(request, this, outputStream));
    }
}
