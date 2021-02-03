/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Http11Request.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.utils.PostInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
final class HttpResponseImpl extends AbstractResponse {
    /**
     * 空流
     */
    private static final InputStream EMPTY_INPUT_STREAM = new InputStream() {
        @Override
        public int read() {
            return -1;
        }
    };
    private InputStream inputStream;

    HttpResponseImpl(Response request) {
        init(request);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        int contentLength = getContentLength();
        if (contentLength <= 0 || response.getFormUrlencoded() != null) {
            inputStream = EMPTY_INPUT_STREAM;
        } else {
            inputStream = new PostInputStream(response.getAioSession().getInputStream(contentLength), contentLength);
        }
        return inputStream;
    }


    public void reset() {
        getResponse().reset();
        response.reset();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
    }

}
