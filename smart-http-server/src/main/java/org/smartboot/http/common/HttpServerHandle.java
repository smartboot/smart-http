/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerHandle.java
 * Date: 2021-02-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;

/**
 * Http消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class HttpServerHandle extends Handle<HttpRequest, HttpResponse> {
}