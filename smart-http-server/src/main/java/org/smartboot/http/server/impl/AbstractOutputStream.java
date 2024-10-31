/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.io.BufferOutputStream;
import org.smartboot.http.common.utils.Constant;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
abstract class AbstractOutputStream extends BufferOutputStream {
    protected final AbstractResponse response;
    protected final CommonRequest request;

    public AbstractOutputStream(CommonRequest request, AbstractResponse response) {
        super(request.getAioSession().writeBuffer());
        this.response = response;
        this.request = request;
    }

    /**
     * 输出Http消息头
     */
    protected void writeHeader(HeaderWriteSource source) throws IOException {
        if (committed) {
            return;
        }

        //转换Cookie
        convertCookieToHeader();

        boolean hasHeader = hasHeader();
        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        writeBuffer.write(getHeadPart(hasHeader));
        if (hasHeader) {
            //输出Header部分
            writeHeaders();
        }
        committed = true;
    }

    protected abstract byte[] getHeadPart(boolean hasHeader);

    private void convertCookieToHeader() {
        List<Cookie> cookies = response.getCookies();
        if (cookies.size() > 0) {
            cookies.forEach(cookie -> response.addHeader(HeaderNameEnum.SET_COOKIE.getName(), cookie.toString()));
        }
    }

    protected boolean hasHeader() {
        return response.getHeaders().size() > 0;
    }

    private void writeHeaders() throws IOException {
        for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
            HeaderValue headerValue = entry.getValue();
            while (headerValue != null) {
                writeBuffer.write(getHeaderNameBytes(entry.getKey()));
                writeBuffer.write(getBytes(headerValue.getValue()));
                writeBuffer.write(Constant.CRLF_BYTES);
                headerValue = headerValue.getNextValue();
            }
        }
        writeBuffer.write(Constant.CRLF_BYTES);
    }
}
