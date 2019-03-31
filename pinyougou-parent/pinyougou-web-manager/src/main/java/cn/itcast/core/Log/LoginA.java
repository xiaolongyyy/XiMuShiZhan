package cn.itcast.core.Log;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


public class LoginA {


    public Map name(HttpServletRequest request){
        String name = SecurityContextHolder.getContext().getAuthentication()
                .getName();

        HashMap hashMap = new HashMap();
        System.out.println(name);
        User attribute = (User) request.getSession().getAttribute("");
        hashMap.put("userName",name);


        return hashMap;
    }
}
