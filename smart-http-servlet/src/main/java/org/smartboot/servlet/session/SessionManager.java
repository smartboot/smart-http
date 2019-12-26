package org.smartboot.servlet.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/21
 */
public interface SessionManager {

    public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    public static final String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";

    public HttpSession getSession(HttpServletRequest request);

    public HttpSession createSession();
}
