package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Destination smsDestination;
@Autowired
private UserDao userDao;
    @Override
    public void sendCode(final String phone) {
        //创建一个6位数验证码
        final String randomNumeric = RandomStringUtils.randomNumeric(6);
        System.out.println(randomNumeric);
        //存入缓存中
        redisTemplate.boundValueOps(phone).set(randomNumeric);
        //发消息
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                //传入手机号码
                mapMessage.setString("iphone",phone);
                //传入验证码
                mapMessage.setString("templateParam","{'code':'"+randomNumeric+"'}");
                //设置商城签名
                mapMessage.setString("signName","晓龙商城");
                //设置模板Id
                mapMessage.setString("templateCode","SMS_161320341");

                return mapMessage;
            }
        });
    }
    //添加
    @Override
    public void add(User user,String smscode) {
        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        //判断验证码是否失效
        if (code==null){
           throw  new RuntimeException("验证码失效");
        }
        if (code.equals(smscode)){
            user.setCreated(new Date());
            user.setUpdated(new Date());
            userDao.insertSelective(user);
        }else {
            throw new RuntimeException("验证码错误");
        }


    }
}
