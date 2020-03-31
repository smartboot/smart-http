/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: SetRequest.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public interface RequestBuilder {
    void setMethod(String method);

    void setUri(String uri);

    void setQueryString(String queryString);

    void setProtocol(String protocol);

    void setHeader(String name, String value);

    void setFormUrlencoded(String formUrlencoded);
}
