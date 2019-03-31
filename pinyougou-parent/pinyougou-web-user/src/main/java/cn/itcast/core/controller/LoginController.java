package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
//回显类查出页面上的名字进行回显操作
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map showname(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        Map map = new HashMap();

        map.put("loginName",name);
        return map;
    }


}
