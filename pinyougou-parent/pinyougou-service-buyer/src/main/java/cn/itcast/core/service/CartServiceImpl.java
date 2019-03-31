package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemDao itemDao;

    //查询单个购物车商品集
    @Override
    public Item findItemById(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }

    //将购物车装满
    //因为传入的时一个购物车集合所以遍历装满即可
    @Override
    public List<Cart> findCartList(List<Cart> cartList) {
        for (Cart cart : cartList) {
            //获取购物商品结果集
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item itemById = findItemById(orderItem.getItemId());
                //显示的图片，标题，价格 小计
                orderItem.setPicPath(itemById.getImage());
                orderItem.setPrice(itemById.getPrice());
                orderItem.setTitle(itemById.getTitle());
                orderItem.setTotalFee(new BigDecimal(itemById.getPrice().doubleValue() * orderItem.getNum()));

                cart.setSellerName(itemById.getSeller());
            }

        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    //登陆时保存到缓存中
    @Override
    public void addCartListToRedis(List<Cart> cartList, String name) {
        // 存入缓存中用的时hash
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("cart").get(name);

        oldCartList = mergeCartList(cartList,oldCartList);

        redisTemplate.boundHashOps("cart").put(name,oldCartList);
    }

    //查询缓存的
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        List<Cart> cart = (List<Cart>) redisTemplate.boundHashOps("cart").get(name);
        return cart;
    }

    //封装一个合并的方法
    public List<Cart> mergeCartList(List<Cart> newcartList, List<Cart> oldCartList) {
        if (newcartList != null && newcartList.size() > 0) {
            if (oldCartList != null && oldCartList.size() > 0) {
                //将新的集合合并到老的集合中
                for (Cart cart : newcartList) {
                    int i = oldCartList.indexOf(cart);
                    if (i != -1) {
                        //根据id查询在老车中存在的商品
                        Cart ordcart = oldCartList.get(i);
                        List<OrderItem> orderItemList = ordcart.getOrderItemList();

                        //查出新车中的结果集
                        List<OrderItem> neworderItemList = cart.getOrderItemList();
                        for (OrderItem neworderItem : neworderItemList) {
                            int i1 = orderItemList.indexOf(neworderItem);
                            if (i1!=-1){
                                //存在合并
                                OrderItem orderItem = orderItemList.get(i1);
                                orderItem.setNum(orderItem.getNum()+neworderItem.getNum());
                            }else {
                                orderItemList.add(neworderItem);
                            }
                        }
                    }else {
                        oldCartList.add(cart);
                    }
                }
            }else {
                return newcartList;
            }
        }

        return oldCartList;
    }
}
