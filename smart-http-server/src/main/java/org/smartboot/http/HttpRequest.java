package org.smartboot.http;

import org.smartboot.http.enums.MethodEnum;

import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/7
 */
public interface HttpRequest {

    String getHeader(String headName);

    InputStream getInputStream();

    String getRequestURI();

    void setRequestURI(String uri);

    String getProtocol();

    public MethodEnum getMethodRange();

    String getOriginalUri();

    void setQueryString(String queryString);
}
