package org.smartboot.http.demo.controller;

import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;

@Controller("controller1")
public class Controller1 {
    @RequestMapping("/helloworld")
    public String helloworld() {
        return "hello " + Controller1.class.getSimpleName();
    }
}
