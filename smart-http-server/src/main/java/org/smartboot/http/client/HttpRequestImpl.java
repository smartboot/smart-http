/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Http11Response.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.socket.transport.WriteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
class HttpRequestImpl extends AbstractRequest {

    public HttpRequestImpl(WriteBuffer outputStream) {
        init(new HttpOutputStream(this, outputStream));
    }
}
