package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.GoodsVo;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.*;

/*
 * 商品管理页面
 * */
@Service
public class GoodsServerImpl implements GoodsServer {
    //引入需要配置的文件

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add(GoodsVo vo) {
        //自定义方法查询你的公司信息
        Seller seller = sellerDao.selectByName(vo.getGoods().getSellerId());
        //商品表添加
        //页面传递过来的手动+时间
        vo.getGoods().setAuditStatus("0");
        vo.getGoods().setSellerId(seller.getSellerId());
        goodsDao.insertSelective(vo.getGoods());
        vo.getGoodsDesc().setGoodsId(vo.getGoods().getId());
        goodsDescDao.insertSelective(vo.getGoodsDesc());

        //判断商品是否下架
        if (vo.getGoods().getIsEnableSpec().equals("1")) {
            for (Item item : vo.getItemList()) {
                String spec = item.getSpec();

                String title = vo.getGoods().getGoodsName();

                Map<String, String> map = JSON.parseObject(spec, Map.class);

                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);
                //商品图片的保存
                String itemImages = vo.getGoodsDesc().getItemImages();
                List<Map> images = JSON.parseArray(itemImages, Map.class);
                if (images != null && images.size() > 0) {
                    item.setImage((String) images.get(0).get("url"));
                }
                //商品三级分类
                item.setCategoryid(vo.getGoods().getCategory3Id());
                //添加三级分类名称
                item.setCategory(itemCatDao.selectByPrimaryKey(vo.getGoods().getCategory3Id()).getName());
                //添加时间
                item.setCreateTime(new Date());
                //添加更新的时间
                item.setUpdateTime(new Date());
                //添加商品的ID
                item.setGoodsId(vo.getGoods().getId());
                //添加商家ID
//                System.out.println(seller.getNickName());
//                System.out.println(seller.getSellerId());
                item.setSellerId(seller.getSellerId());

                //添加商家名称   商家的名称是从数据库中查询到的
                item.setSeller(seller.getNickName());
                //品牌名称    从数据空中查询的品牌名称
                item.setBrand(brandDao.selectByPrimaryKey(vo.getGoods().getBrandId()).getName());

                //保存数据
                itemDao.insertSelective(item);
            }
        } else {
            //如果没上架不需要写
        }
    }

    @Override
    public String Cnie(String name) {
        Seller seller = sellerDao.selectByName(name);
        return seller.getSellerId();
    }

    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page, rows);

        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if (goods.getSellerId() != null) {
            criteria.andSellerIdEqualTo(goods.getSellerId());
        }
        if (goods.getAuditStatus() != null && !goods.getAuditStatus().equals("")) {
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
        if (goods.getGoodsName() != null && !goods.getGoodsName().trim().equals("")) {
            criteria.andGoodsNameLike("%" + goods.getGoodsName().trim() + "%");
        }
        criteria.andIsDeleteIsNull();
        Page<Goods> pages = (Page<Goods>) goodsDao.selectByExample(goodsQuery);

        return new PageResult(pages.getTotal(), pages.getResult());
    }

    /*
    用于回显数据
    因为回显数据是一个类型的集合
    * */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        Goods goods = goodsDao.selectByPrimaryKey(id);
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(itemQuery);
        //往对象中添加数据
        goodsVo.setGoods(goods);
        goodsVo.setGoodsDesc(goodsDesc);
        goodsVo.setItemList(items);
        return goodsVo;
    }

    @Override
    public void update(GoodsVo goodsVo) {
        //自定义方法查询商品信息
        Seller seller = sellerDao.selectByName(goodsVo.getGoods().getSellerId());
        //修改取出基本信息如果

        Goods goods = goodsVo.getGoods();
        goods.setSellerId(seller.getSellerId());
        goodsDao.updateByPrimaryKeySelective(goods);
        //修改goosDesc表单需要
        goodsDescDao.updateByPrimaryKeySelective(goodsVo.getGoodsDesc());

        //修改
        //先删然后执行删除的操作
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(itemQuery);

        //添加操作
        List<Item> itemList = goodsVo.getItemList();
        //判断商品是否下架
        if (goods.getIsEnableSpec().equals("1")) {
            for (Item item : itemList) {
                //商品规格添加
                String spec = item.getSpec();
                String title = goods.getGoodsName();
                //给商品规格加上规格
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();

                }
                item.setTitle(title);
                //获取照片的路径

                String image = goodsVo.getGoodsDesc().getItemImages();
                List<Map> images = JSON.parseArray(image, Map.class);
                if (images.size() > 0 && images != null) {
                    item.setImage((String) images.get(0).get("url"));
                }
                //添加商品的三级分类
                item.setCategoryid(goods.getCategory3Id());
                //添加商品的三级分类的名称
                item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());
                //添加时间
                item.setCreateTime(new Date());
                //添加更新时间
                item.setUpdateTime(new Date());
                //添加商品ID
                item.setGoodsId(goods.getId());
                //添加商家ID
                item.setSellerId(seller.getSellerId());
                //添加商品的名称
                item.setSeller(seller.getNickName());
                //添加品牌的名称
                item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());
                itemDao.insertSelective(item);

            }
        } else {
//            不做任何操作
        }

    }
@Autowired
private Destination queueSolrDeleteDestination;
    @Override
    public void delete(Long[] ids) {
        Goods goods = new Goods();
        goods.setIsDelete("1");
        if (ids != null) {
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                //删除solr索引苦衷的内容
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;

                    }
                });

            }
        }

    }

    /*
     * 修改审核的操作
     *
     * */
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;
    @Override
    public void updateStatus(Long[] ids, String status) {
        Goods goods = new Goods();
        goods.setAuditStatus(status);
        if (ids != null) {
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);

                if ("1".equals(status)) {

                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                            return textMessage;
                        }
                    });


                }

            }
        }

    }

//    public void importList(Long[] ids) {
//        ItemQuery itemQuery = new ItemQuery();
//
//        itemQuery.createCriteria().andStatusEqualTo("1")
//                .andGoodsIdIn(Arrays.asList(ids));
//
//        List<Item> items = itemDao.selectByExample(itemQuery);
//        solrTemplate.saveBeans(items,1000);
//
//
//    }


}
