/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Pipeline.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http;

import org.smartboot.http.server.handle.HttpHandle;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public interface Pipeline {
    Pipeline next(HttpHandle nextHandle);
}
