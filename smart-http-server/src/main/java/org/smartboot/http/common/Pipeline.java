/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Pipeline.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

/**
 * 消息处理管道
 *
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public interface Pipeline<REQ, RSP> {
    Pipeline<REQ, RSP> next(Handle<REQ, RSP> nextHandle);
}
