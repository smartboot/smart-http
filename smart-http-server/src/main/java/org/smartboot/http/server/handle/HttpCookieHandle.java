/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpCookieHandle.java
 * Date: 2020-11-08
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handle;

import org.smartboot.http.Cookie;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.Cookies;
import org.smartboot.http.utils.HttpHeaderConstant;

import java.io.IOException;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/8
 */
public class HttpCookieHandle extends HttpHandle {
    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        String cookieValue = request.getHeader(HttpHeaderConstant.Names.COOKIE);
        Map<String, Cookie> cookieMap = Cookies.parseRequestCookies(true, cookieValue);

    }
}
