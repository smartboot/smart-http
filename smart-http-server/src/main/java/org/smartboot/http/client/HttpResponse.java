/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequest.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.Cookie;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Http消息请求接口
 *
 * @author 三刀
 * @version V1.0 , 2018/8/7
 */
public interface HttpResponse {
    String HTTP_1_0_STRING = "HTTP/1.0";
    String HTTP_1_1_STRING = "HTTP/1.1";

    /**
     * 获取指定名称的Http Header值
     *
     * @param headName
     * @return
     */
    String getHeader(String headName);


    public Collection<String> getHeaders(String name);

    Collection<String> getHeaderNames();

    InputStream getInputStream() throws IOException;

    String getProtocol();

    String getContentType();

    int getContentLength();

    /**
     * Returns the Internet Protocol (IP) address of the client
     * or last proxy that sent the request.
     * For HTTP servlets, same as the value of the
     * CGI variable <code>REMOTE_ADDR</code>.
     *
     * @return a <code>String</code> containing the
     * IP address of the client that sent the request
     */
    String getRemoteAddr();

    InetSocketAddress getRemoteAddress();

    InetSocketAddress getLocalAddress();

    /**
     * Returns the fully qualified name of the client
     * or the last proxy that sent the request.
     * If the engine cannot or chooses not to resolve the hostname
     * (to improve performance), this method returns the dotted-string form of
     * the IP address. For HTTP servlets, same as the value of the CGI variable
     * <code>REMOTE_HOST</code>.
     *
     * @return a <code>String</code> containing the fully
     * qualified name of the client
     */
    String getRemoteHost();

    public Locale getLocale();

    public Enumeration<Locale> getLocales();

    String getCharacterEncoding();

    /**
     * 获取Cookie
     */
    Cookie[] getCookies();


    /**
     * 获取附件对象
     *
     * @param <A> 附件对象类型
     * @return 附件
     */
    <A> A getAttachment();

    /**
     * 存放附件，支持任意类型
     *
     * @param <A>        附件对象类型
     * @param attachment 附件对象
     */
    <A> void setAttachment(A attachment);
}
