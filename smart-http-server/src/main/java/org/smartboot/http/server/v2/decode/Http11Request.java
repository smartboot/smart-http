package org.smartboot.http.server.v2.decode;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.enums.MethodEnum;
import org.smartboot.http.enums.State;
import org.smartboot.http.utils.Consts;
import org.smartboot.socket.extension.decoder.DelimiterFrameDecoder;

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

    boolean tmpValEnable = false;
    DelimiterFrameDecoder tmpHeaderValue = new DelimiterFrameDecoder(new byte[]{Consts.CR}, 1024);


    private String requestUri;

    @Override
    public String getHeader(String headName) {
        return headMap.get(headName);
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public void setRequestURI(String uri) {
        this.requestUri = uri;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public MethodEnum getMethodRange() {
        return methodEnum;
    }

    @Override
    public String getOriginalUri() {
        return originalUri;
    }

    @Override
    public void setQueryString(String queryString) {

    }
    public void rest() {
        state = State.method;
        headMap.clear();
        tmpHeaderName = null;
        tmpValEnable=false;
        tmpHeaderValue.reset();
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
