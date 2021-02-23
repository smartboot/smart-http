/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BasicAuthServerHandle.java
 * Date: 2021-02-23
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handle;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;

import java.io.IOException;
import java.util.Base64;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/23
 */
public final class BasicAuthServerHandle extends HttpServerHandle {
    private final HttpServerHandle httpServerHandle;
    private final String basic;

    public BasicAuthServerHandle(String username, String password, HttpServerHandle httpServerHandle) {
        this.httpServerHandle = httpServerHandle;
        basic = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        String clientBasic = request.getHeader(HttpHeaderConstant.Names.AUTHORIZATION);
        if (StringUtils.equals(clientBasic, this.basic)) {
            httpServerHandle.doHandle(request, response);
        } else {
            response.setHeader(HttpHeaderConstant.Names.WWW_AUTHENTICATE, "Basic realm=\"smart-http\"");
            response.setHttpStatus(HttpStatus.UNAUTHORIZED);
        }
    }

}
