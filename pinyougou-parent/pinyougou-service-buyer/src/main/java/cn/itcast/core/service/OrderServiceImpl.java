package cn.itcast.core.service;

import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
* 提交订单保存
* */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private PayLogDao payLogDao;

    @Override
    public void add(Order order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cart").get(order.getUserId());
        Long pagtotal=0l;
        List<Long> longs = new ArrayList<>();
        //遍历购物车
        for (Cart cart : cartList) {
            //添加唯一标识
            long ll = idWorker.nextId();
            longs.add(ll);
            order.setOrderId(ll);
            //添加支付状态
            order.setStatus("1");
            //添加创建时间更新时间
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            //设置订单来源
            order.setSourceType("2");
            //设置商家id
            order.setSellerId(cart.getSellerId());
            //实付金额
            double total = 0;
            //购物车列表从缓存中查询
            //获取商品的结果集kl?
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                //根据item查询产品详情
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                long l = idWorker.nextId();
                //增加唯一标识
                orderItem.setId(l);
                //订单id
                orderItem.setOrderId(ll);
                //添加商品id
                orderItem.setGoodsId(item.getGoodsId());
                //标题
                orderItem.setTitle(item.getTitle());
                //价格
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));
                //图片
                orderItem.setPicPath(item.getImage());
                //商家id
                orderItem.setSellerId(item.getSellerId());
                //钱
                total+=orderItem.getTotalFee().doubleValue();

                //保存
                orderItemDao.insertSelective(orderItem);
            }

            order.setPayment(new BigDecimal(total));
            pagtotal+=order.getPayment().longValue();
            orderDao.insertSelective(order);

        }
        //创建日志表
        PayLog payLog = new PayLog();
        //设置价格
        payLog.setTotalFee(pagtotal*100);
        //用户账号
        payLog.setUserId(order.getUserId());
        //设置id
        payLog.setOutTradeNo(String .valueOf(idWorker.nextId()));
        //设置时间 创造  支付时间
        payLog.setCreateTime(new Date());
        payLog.setPayTime(new Date());
        //设置事务id
        //payLog.setTransactionId();
        //设置支付状态  未支付
        payLog.setTradeState("0");
        //设置订单号
        String orders="";
        for (Long aLong : longs) {
            orders+=aLong+",";
        }
        orders.substring(0,orders.length()-1);
        payLog.setOrderList(orders);
        //设置支付方式 微信支付
        payLog.setPayType("1");
        //存入日志数据库中
        payLogDao.insertSelective(payLog);
        //存入缓存中
        redisTemplate.boundHashOps("paylog").put(order.getUserId(),payLog);

        //清空缓存
        redisTemplate.boundHashOps("cart").delete(order.getUserId());
    }
}
