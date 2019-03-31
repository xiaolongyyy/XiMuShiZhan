package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Cart;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/*
    解决跨域问题
    跨域接收但是不可以响应
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @CrossOrigin(origins="http://localhost:8723",allowCredentials="true")
    @RequestMapping("/addGoodsToCartList")
    //接受的参数 商品id 数量  存入cookies中
    public Result addGoodsToCartList(HttpServletRequest request, HttpServletResponse response,Long itemId,Integer num){
        try {
            //提升作用域
            List<Cart> cartList=null;
            //获取cookie
            Cookie[] cookies = request.getCookies();
            //获取cookie中的购物车 判断cookis不为空
            //如果有购物车则获取cookie中的购物车

            if (null!=cookies&&cookies.length>0){
                for (Cookie cookie : cookies) {
                    //判断cookie中是否有需要的属性
                    if ("CART".equals(cookie.getName())){
                        String value = cookie.getValue();
                        value = URLDecoder.decode(value, "UTF-8");
                         cartList = JSON.parseArray(value, Cart.class);
                    }
                    
                }
            }
            //判断如果没有则创建购物车
            if (cartList==null){
               cartList=new ArrayList<>();
            }
            //追加当前款
            //把你选中的款式加入购物车
            Cart newCart = new Cart();
            //从数据库查询当前款的数据
            Item item=cartService.findItemById(itemId);
            //存入cart中 存入商品id    id存  名字不存 商品结果集不存
            newCart.setSellerId(item.getSellerId());
            //存入库存结果集
            OrderItem newOrderItem = new OrderItem();
            //存入id数量
            newOrderItem.setItemId(itemId);
            newOrderItem.setNum(num);
            //创建储存的集合 把购物车存入cart中
            List<OrderItem> newOrderItemList = new ArrayList<>();
            newOrderItemList.add(newOrderItem);
            newCart.setOrderItemList(newOrderItemList);
            // 判断当前商家是否在购物车中
            //indexOf作用判断是否包含如果有返回角标及正数没有返回-1
            int i = cartList.indexOf(newCart);

            if (i!=-1){
                //如果购物车包含这个商品
                Cart cart = cartList.get(i);
                List<OrderItem> orderItemList = cart.getOrderItemList();
                //判断商家中书否存在这个商品
                int i2 = orderItemList.indexOf(newOrderItem);
                if (i2!=-1){
                    //存在则追加商品数量
                    for (OrderItem oldOrderItem : orderItemList) {
                        oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());
                    }

                }else {
                    //不存在直接储存商品即可
                    orderItemList.add(newOrderItem);
                }
            }else {
                //商家不存在存入商家即可
                cartList.add(newCart);
            }
            //判断有无登陆人
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //anonymousUser是默认登陆名
            if (!"anonymousUser".equals(name)){
                //已登录
                cartService.addCartListToRedis(cartList,name);
                Cookie cookie = new Cookie("CART", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

            }else {
                //未登录
                String s = JSON.toJSONString(cartList);
                s = URLEncoder.encode(s, "utf-8");
                //存入Cookie中
                Cookie cookie = new Cookie("CART",s);
                //设置保存时间
                cookie.setMaxAge(60*60*24);
                //设置作用范围
                cookie.setPath("/");

                response.addCookie(cookie);

            }
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }

    }
    //查询购物车的商品
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException {
        //创建一个空的商品集合
        List<Cart>cartList=null;
        //从cookies中获取数据
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            //循环遍历如果存在则覆盖返回即可
            for (Cookie cookie : cookies) {
              if ("CART".equals(cookie.getName())){
                  String value = cookie.getValue();
                  value = URLDecoder.decode(value, "UTF-8");
                  cartList = JSON.parseArray(value, Cart.class);
              }

            }
        }
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!"anonymousUser".equals(name)){
            if (cartList!=null){
                cartService.addCartListToRedis(cartList,name);
                Cookie cookie = new Cookie("CART", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            cartList= cartService.findCartListFromRedis(name);


        }
        //如果有数据 则把空缺 的数据补充明白
        if (cartList!=null){
            cartList=cartService.findCartList(cartList);
        }
        return cartList;
    }


}
