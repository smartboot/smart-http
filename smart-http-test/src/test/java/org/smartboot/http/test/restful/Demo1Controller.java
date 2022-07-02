package org.smartboot.http.test.restful;

import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;

@Controller
class Demo1Controller {

    @RequestMapping
    public String test1() {
        return "hello";
    }
}