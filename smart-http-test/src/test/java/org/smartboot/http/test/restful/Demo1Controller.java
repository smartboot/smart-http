package org.smartboot.http.test.restful;

import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Controller
class Demo1Controller {

    @PostConstruct
    public void init() {
        System.out.println("init");
    }

    @RequestMapping
    public String test1() {
        return "hello";
    }


    @PreDestroy
    public void destroy() {
        System.out.println("destroy");
    }
}