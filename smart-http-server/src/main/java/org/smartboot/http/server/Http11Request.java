/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Http11Request.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.utils.EmptyInputStream;
import org.smartboot.http.utils.PostInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public final class Http11Request extends AbstractRequest {

    private InputStream inputStream;

    private Http11Response response;

    Http11Request(BaseHttpRequest request) {
        init(request);
        this.response = new Http11Response(this, request.getAioSession().writeBuffer());
    }

    public final Http11Response getResponse() {
        return response;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        if (!HttpMethodEnum.POST.getMethod().equalsIgnoreCase(getMethod())) {
            inputStream = new EmptyInputStream();
        } else if (request.getFormUrlencoded() == null) {
            inputStream = new PostInputStream(request.getAioSession().getInputStream(getContentLength()), getContentLength());
        } else {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return inputStream;
    }


    public void reset() {
        super.reset();
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
