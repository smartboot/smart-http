/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: CloseableResponse.java
 * Date: 2020-04-23
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.HttpResponse;

/**
 * @author 三刀
 * @version V1.0 , 2020/4/23
 */
interface CloseableResponse extends HttpResponse {

    /**
     * 消息处理完毕后是否期望关闭Socket通道
     *
     * @return
     */
    boolean isChannelClosed();
}
