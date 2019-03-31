package cn.itcast.core.controller;

import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.service.OrderService;
import cn.itcast.core.service.WeixinPayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;


    @RequestMapping("/createNative")
    public Map createNative() {
        //生成唯一标识
        IdWorker idWorker = new IdWorker();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return weixinPayService.createNative(name);

    }
@Reference
private OrderService orderService;
    //查询订单状态是否支付
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            int x = 0;
            while (true) {
                Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no,name);
                System.out.println(map.size());
                if ("NOTPAY".equals(map.get("trade_state"))) {
                    Thread.sleep(5000);
                    x++;
                if (x>=60){
                    return new Result(false,"验证码超时");
                }
                }else {
                    //orderService.updateState();
                   return new Result(true,"支付成功");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new Result(false,"验证内部错误");
        }


    }
}
