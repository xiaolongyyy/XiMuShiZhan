package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import javax.servlet.ServletContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 生成商品详情页面
 *
 * */
@Service
public class ItemPageServiceImpl implements ItemPageService, ServletContextAware {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private ItemCatDao itemCatDao;

    //静态化商品详情
    public void index(Long id) {
        //创建Freemarker
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //输出流
        Writer writer = null;
        //输出路径
        String path = getPath("/" + id + ".html");
        //加载模板
        //给静态页面设置名字
        try {
            Template template = configuration.getTemplate("item.ftl");
            // 输出流到磁盘中 UTP-8在spring中配置
            writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
//            创建数据进行输出
            Map<String, Object> root = new HashMap<>();
//            根据商品id查询结果集
            ItemQuery itemQuery = new ItemQuery();
            itemQuery.createCriteria().andGoodsIdEqualTo(id);
            List<Item> itemList = itemDao.selectByExample(itemQuery);
            root.put("itemList", itemList);

            GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
            root.put("goodsDesc", goodsDesc);

            Goods goods = goodsDao.selectByPrimaryKey(id);
            root.put("goods", goods);

            root.put("itemCat1", itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName());
            root.put("itemCat2", itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName());
            root.put("itemCat3", itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());

//            处理数据
            template.process(root,writer);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    //获取全路径
    public String getPath(String path) {
        return servletContext.getRealPath(path);
    }

    //    注入获得路径的ServletContext
    private ServletContext servletContext;

    //实现接口并赋值给变量
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
