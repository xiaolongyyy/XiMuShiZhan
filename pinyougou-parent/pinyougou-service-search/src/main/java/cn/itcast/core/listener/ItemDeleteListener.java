package cn.itcast.core.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;
    //接收消息的方法
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm=(ActiveMQTextMessage)message;
        try {
            String id = atm.getText();
            System.out.println(id);
//            将商品从索引库中删除

            SimpleQuery item_goodsid = new SimpleQuery(new Criteria("item_goodsid").is(id));
            solrTemplate.delete(item_goodsid);
            solrTemplate.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
