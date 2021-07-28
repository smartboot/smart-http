/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BodyStream.java
 * Date: 2021-07-17
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/17
 */
public interface BodyStream {
    BodyStream write(byte[] bytes, int offset, int len);

    BodyStream flush();
}
