package cn.itcast.core.service;

import cn.itcast.common.utils.HttpClient;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService{
    //公众号
    @Value("${appid}")
    private String appid;
    //商户号
    @Value("${partner}")
    private String partner;
    //签名
    @Value("${partnerkey}")
    private String partnerkey;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map createNative(String name) {

        PayLog paylog = (PayLog) redisTemplate.boundHashOps("paylog").get(name);
        Map<String,String> map = new HashMap();
        //公众号
        map.put("appid",appid);
        //随字符串
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        //商户号
        map.put("mch_id",partner);
        //商品描述
        map.put("body","你快给钱我传智是骗子");
        //商户订单号
        map.put("out_trade_no", paylog.getOutTradeNo());
        //总金额
//        map.put("total_fee",paylog.getTotalFee()+"");
        map.put("total_fee","1");
        //IP
        map.put("spbill_create_ip", "127.0.0.1");
        //回调地址
        map.put("notify_url", "http://www.itcast.cn");
        //交易类型
        map.put("trade_type", "NATIVE");

        try {
            String xmlParam = WXPayUtil.generateSignedXml(map, partnerkey);
            System.out.println(xmlParam);

            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            String content = client.getContent();
            System.out.println(client);

            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            if ("SUCCESS".equals(resultMap.get("return_code"))){
                resultMap.put("code_url", resultMap.get("code_url"));
                resultMap.put("total_fee", String.valueOf(paylog.getTotalFee()));
                resultMap.put("out_trade_no", paylog.getOutTradeNo());
                return resultMap;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Autowired
    private OrderDao orderDao;

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no,String name) {

        //创建路径
        String url="https://api.mch.weixin.qq.com/pay/orderquery";
        //发送请求、接收响应
        HttpClient httpClient = new HttpClient(url);
        //设置为https
        httpClient.setHttps(true);

        Map<String, String> param = new HashMap<>();
        //公众账号id
        param.put("appid",appid);
        //商户号
        param.put("mch_id",partner);
        //订单号
        param.put("out_trade_no",out_trade_no);
        //随机字符串
        param.put("nonce_str",WXPayUtil.generateNonceStr());


        //返回结果为xml形式
        try {
            String xml = WXPayUtil.generateSignedXml(param,partnerkey);
            //设置入参
            httpClient.setXmlParam(xml);
            //请求
            httpClient.post();
            //响应
            String content = httpClient.getContent();

            Map<String, String> map = WXPayUtil.xmlToMap(content);
            //判断支付状态
            if ("SUCCESS".equals(map.get("trade_state"))){
                updateOrderStatus(map.get("transaction_id"),name);

            }
            return map;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
    @Autowired
    private PayLogDao payLogDao;
    //更新事务
    public void updateOrderStatus(String transaction_id, String name) {
        //获取缓存中日志
        PayLog paylog = (PayLog) redisTemplate.boundHashOps("paylog").get(name);
        String[] orderList = paylog.getOrderList().split(",");
        Order order = new Order();
        //修改订单为已支付
        order.setStatus("2");
        //修改时间  付款时间
        order.setUpdateTime(new Date());
        order.setPaymentTime(new Date());
        //根据商品id进行修改
        for (String id : orderList) {
            order.setOrderId(Long.parseLong(id));
            orderDao.updateByPrimaryKeySelective(order);
        }
        //修改日志
        //修改支付时间
        paylog.setPayTime(new Date());
        //修改流水
        paylog.setTransactionId(transaction_id);
        //修改状态
        paylog.setTradeState("2");
        payLogDao.updateByPrimaryKeySelective(paylog);

        //清除日志缓存
        redisTemplate.boundHashOps("paylog").delete(paylog.getUserId());

    }
}
