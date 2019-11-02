package org.smartboot.http;

import org.smartboot.http.enums.MethodEnum;

import java.io.InputStream;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/7
 */
public interface HttpRequest {

    /**
     * 获取指定名称的Http Header值
     *
     * @param headName
     * @return
     */
    String getHeader(String headName);

    Map<String, String> getHeaders();


    InputStream getInputStream();

    String getRequestURI();

    void setRequestURI(String uri);

    String getProtocol();

    MethodEnum getMethodRange();

    String getOriginalUri();

    void setQueryString(String queryString);

    String getContentType();

    int getContentLength();

    String getParameter(String name);

    String[] getParameterValues(String name);

    Map<String, String[]> getParameters();
}
