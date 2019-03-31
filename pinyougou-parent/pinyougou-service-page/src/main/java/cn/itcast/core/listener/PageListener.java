package cn.itcast.core.listener;

import cn.itcast.core.service.ItemPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/*
* 自定义消息处理类
* */
public class PageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    //接收消息的方法
    @Override
    public void onMessage(Message message) {
       ActiveMQTextMessage activeMQTextMessage= (ActiveMQTextMessage)message;

        try {
            String id = activeMQTextMessage.getText();
            System.out.println("静态化项目接收的ID:"+id);
        //静态化模板
            itemPageService.index(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
