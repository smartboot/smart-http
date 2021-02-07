/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestHook.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/31
 */
interface RequestHook {
    Request getRequest();
}
