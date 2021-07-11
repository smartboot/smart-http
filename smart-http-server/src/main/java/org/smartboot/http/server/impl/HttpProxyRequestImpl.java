/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequestImpl.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
class HttpProxyRequestImpl extends HttpRequestImpl {

    private boolean connected = false;

    private InputStream proxyInputStream = EMPTY_INPUT_STREAM;

    HttpProxyRequestImpl(Request request) {
        super(request);
    }

    public void setProxyInputStream(InputStream proxyInputStream) {
        this.proxyInputStream = proxyInputStream;
    }

    @Override
    public InputStream getInputStream() {
        return proxyInputStream;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void reset() {
    }
}
