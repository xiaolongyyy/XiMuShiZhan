package cn.itcast.core.service;

import java.util.Map;

public interface WeixinPayService {
    Map createNative(String name);

    Map<String,String> queryPayStatus(String out_trade_no,String name);
}
