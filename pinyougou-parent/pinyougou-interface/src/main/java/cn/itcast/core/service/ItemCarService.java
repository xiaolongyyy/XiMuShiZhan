package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface ItemCarService {
    List<ItemCat> findByParetId(Long parentId);

    void add(ItemCat itemCat);

    ItemCat findOne(Long id);

    Long delete(Long[] ids);


    PageResult search(Integer page, Integer rows, ItemCat itemCat);

    void update(ItemCat itemCat);

    List<ItemCat> findAll();
}
