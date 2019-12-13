package org.smartboot.servlet.war;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.smartboot.http.utils.NumberUtils;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.conf.ErrorPageInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.conf.WebAppInfo;
import org.smartboot.servlet.enums.MappingType;

import javax.servlet.DispatcherType;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/12
 */
public class WebXmlParse {

    public static void main(String[] args) throws IOException, DocumentException {
        WebXmlParse loader = new WebXmlParse();
        loader.load(new FileInputStream("/Users/zhengjunwei/IdeaProjects/smart-http/smart-http-servlet/src/main/resources/web.xml"));
    }

    public WebAppInfo load(InputStream webXmlStream) throws DocumentException {
        WebAppInfo webAppInfo = new WebAppInfo();
        Document document = new SAXReader().read(webXmlStream);
        Element rootElement = document.getRootElement();

        parseServlet(webAppInfo, rootElement);
        parseServletMapping(webAppInfo, rootElement);

        parseFilter(webAppInfo, rootElement);
        parseFilterMapping(webAppInfo, rootElement);

        parseListener(webAppInfo, rootElement);

        parseContextParam(webAppInfo, rootElement);

        parseErrorPage(webAppInfo, rootElement);

        parseSessionConfig(webAppInfo, rootElement);

        return webAppInfo;
    }

    private void parseSessionConfig(WebAppInfo webAppInfo, Element rootElement) {
        Element sessionElement = rootElement.element("session-config");
        if (sessionElement != null) {
            webAppInfo.setSessionTimeout(NumberUtils.toInt(sessionElement.elementTextTrim("session-timeout"), 0));
        }
    }

    private void parseContextParam(WebAppInfo webAppInfo, Element rootElement) {
        List<Element> servletElementList = rootElement.elements("context-param");
        for (Element element : servletElementList) {
            webAppInfo.addContextParam(element.elementTextTrim("param-name"), element.elementTextTrim("param-value"));
        }
    }

    private void parseListener(WebAppInfo webAppInfo, Element rootElement) {
        List<Element> servletElementList = rootElement.elements("listener");
        for (Element element : servletElementList) {
            webAppInfo.addListener(element.elementTextTrim("listener-class"));
        }
    }


    private void parseFilter(WebAppInfo webAppInfo, Element rootElement) {
        List<Element> servletElementList = rootElement.elements("filter");
        for (Element element : servletElementList) {
            FilterInfo filterInfo = new FilterInfo();
            filterInfo.setFilterName(element.elementTextTrim("filter-name"));
            filterInfo.setFilterClass(element.elementTextTrim("filter-class"));
            Map<String, String> initParamMap = parseParam(element);
            initParamMap.forEach((key, value) -> {
                filterInfo.addInitParam(key, value);
            });
            webAppInfo.addFilter(filterInfo);
        }
    }

    private void parseErrorPage(WebAppInfo webAppInfo, Element rootElement) {
        List<Element> servletElementList = rootElement.elements("error-page");
        for (Element element : servletElementList) {
            Integer errorCode = NumberUtils.toInt(element.elementTextTrim("error-code"), -1);
            if (errorCode < 0) {
                continue;
            }
            webAppInfo.addErrorPage(new ErrorPageInfo(element.elementTextTrim("location"), errorCode, element.elementTextTrim("exception-type")));
        }
    }

    private void parseFilterMapping(WebAppInfo webAppInfo, Element rootElement) {
        List<Element> servletElementList = rootElement.elements("filter-mapping");
        for (Element element : servletElementList) {
            String filterName = element.elementTextTrim("filter-name");
            String urlPattern = element.elementTextTrim("url-pattern");
            String servletName = element.elementTextTrim("servlet-name");
            String dispatcher = element.elementTextTrim("dispatcher");
            if (StringUtils.isBlank(dispatcher)) {
                dispatcher = DispatcherType.REQUEST.name();
            }
            FilterMappingInfo filterInfo = new FilterMappingInfo(filterName
                    , StringUtils.isBlank(urlPattern) ? MappingType.SERVLET : MappingType.URL,
                    StringUtils.isBlank(urlPattern) ? servletName : urlPattern,
                    DispatcherType.valueOf(dispatcher));
            webAppInfo.addFilterMapping(filterInfo);
        }
    }

    /**
     * 解析Servlet配置
     *
     * @param webAppInfo
     * @param rootElement
     */
    private void parseServlet(WebAppInfo webAppInfo, Element rootElement) {
        List<Element> servletElementList = rootElement.elements("servlet");
        for (Element element : servletElementList) {
            ServletInfo servletInfo = new ServletInfo();
            servletInfo.setServletName(element.elementTextTrim("servlet-name"));
            servletInfo.setServletClass(element.elementTextTrim("servlet-class"));
            Map<String, String> initParamMap = parseParam(element);
            initParamMap.forEach((key, value) -> {
                servletInfo.addInitParam(key, value);
            });
            servletInfo.setLoadOnStartup(NumberUtils.toInt(element.elementTextTrim("load-on-startup"), 0));
            webAppInfo.addServlet(servletInfo);
        }
    }

    /**
     * 解析Servlet配置
     *
     * @param webAppInfo
     * @param rootElement
     */
    private void parseServletMapping(WebAppInfo webAppInfo, Element rootElement) {
        List<Element> servletElementList = rootElement.elements("servlet-mapping");
        for (Element element : servletElementList) {
            ServletInfo servletInfo = webAppInfo.getServlet(element.elementTextTrim("servlet-name"));
            servletInfo.addMapping(element.elementTextTrim("url-pattern"));
        }
    }

    private Map<String, String> parseParam(Element rootElement) {
        List<Element> paramElementList = rootElement.elements("init-param");
        Map<String, String> paramMap = new HashMap<>();
        for (Element element : paramElementList) {
            paramMap.put(element.elementTextTrim("param-name"), element.elementTextTrim("param-value"));
        }
        return paramMap;
    }
}
