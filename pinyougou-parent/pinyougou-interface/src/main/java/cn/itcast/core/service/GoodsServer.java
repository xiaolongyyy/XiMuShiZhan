package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import entity.GoodsVo;
import entity.PageResult;

import java.util.List;

public interface GoodsServer {
    void add(GoodsVo goodsVo);

    String Cnie(String name);

    PageResult search(Integer page, Integer rows, Goods goods);


    GoodsVo findOne(Long id);

    void update(GoodsVo goodsVo);

    void delete(Long[] id);

    void updateStatus(Long[] ids,String status);
}
