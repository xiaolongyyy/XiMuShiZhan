package cn.itcast.core.listener;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.ItemSearchService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;

public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;
    @Override
    public void onMessage(Message message) {
        //强转
        ActiveMQTextMessage activeMQTextMessage=(ActiveMQTextMessage)message;
        try {
            String id = activeMQTextMessage.getText();
            System.out.println(id);
            //2:将此商品信息保存到索引库  //商品ID + 是否默认 = 1条 或 4条
                    ItemQuery itemQuery = new ItemQuery();
                    itemQuery.createCriteria().andGoodsIdEqualTo(Long.parseLong(id)).andIsDefaultEqualTo("1");
                    List<Item> itemList = itemDao.selectByExample(itemQuery);
                    solrTemplate.saveBeans(itemList, 1000);

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
