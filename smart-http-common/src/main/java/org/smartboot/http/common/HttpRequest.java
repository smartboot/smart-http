package org.smartboot.http.common;

import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/7
 */
public interface HttpRequest {

    String getHeader(String headName);

    InputStream getInputStream();

    String getRequestURI();
}
