/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpResponse.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import java.util.Collection;

/**
 * Http消息请求接口
 *
 * @author 三刀
 * @version V1.0 , 2018/8/7
 */
public interface HttpResponse {

    /**
     * 获取指定名称的Http Header值
     *
     * @param headName
     * @return
     */
    String getHeader(String headName);


    Collection<String> getHeaders(String name);

    Collection<String> getHeaderNames();

    String getProtocol();

    String getContentType();

    int getContentLength();

    String getCharacterEncoding();

    String body();

    /**
     * 获取响应码
     *
     * @return
     */
    int getStatus();

    /**
     * 获取响应描述
     *
     * @return
     */
    String getReasonPhrase();
}
