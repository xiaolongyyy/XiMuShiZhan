package cn.itcast.core.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LogInController {

    @RequestMapping("/showName")
    public Map<String,Object> showName(HttpServletRequest request){

        SecurityContext attribute = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        Authentication authentication = attribute.getAuthentication();


        User user = (User) authentication.getPrincipal();
        String username = user.getUsername();
        Date date = new Date();

        Map<String, Object> map = new HashMap<>();

        map.put("namee",username);
        map.put("datee",date);

        return map;
    }

}
