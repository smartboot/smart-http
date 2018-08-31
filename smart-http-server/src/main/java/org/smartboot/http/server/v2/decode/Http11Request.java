package org.smartboot.http.server.v2.decode;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.enums.MethodEnum;
import org.smartboot.http.enums.State;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class Http11Request implements HttpRequest {
    State state = State.method;
    MethodEnum methodEnum;
    String originalUri;
    String protocol;
    Map<String, String> headMap = new HashMap<>();

    String tmpHeaderName;

    @Override
    public String getHeader(String headName) {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public void setRequestURI(String uri) {

    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public MethodEnum getMethodRange() {
        return null;
    }

    @Override
    public String getOriginalUri() {
        return null;
    }

    @Override
    public void setQueryString(String queryString) {

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
