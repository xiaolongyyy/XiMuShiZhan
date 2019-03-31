package cn.itcast.core.controller;

import cn.itcast.common.utils.PhoneFormatCheckUtils;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("all")
@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;
    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
        //工具类判断手机号是否合法
        if (PhoneFormatCheckUtils.isPhoneLegal(phone)){
        try {
            userService.sendCode(phone);
            return new Result(true,"验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"验证码错误");
        }

        }else {
            return new Result(false,"手机号码格式错误");
        }
    }
    @RequestMapping("/add")
    public Result add(@RequestBody User user,String smscode){
        try {
            userService.add(user,smscode);
            return new Result(true,"保存成功");
        } catch (RuntimeException e) {
            return new Result(false,e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"注册失败");
        }

    }
}
