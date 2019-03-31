package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ItemCarServiceImpl implements ItemCarService {
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private TypeTemplateDao typeTemplateDao;
    //引入缓存的Rides
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParetId(Long parentId) {
        List<ItemCat> itemCats1 = itemCatDao.selectByExample(null);
        for (ItemCat cat : itemCats1) {
            redisTemplate.boundHashOps("item").put(cat.getName(),cat.getTypeId());

        }
        ItemCatQuery itemCatQuery = new ItemCatQuery();

        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();

        criteria.andParentIdEqualTo(
                parentId);

        List<ItemCat> itemCats = itemCatDao.selectByExample(itemCatQuery);

        return itemCats;


    }

    @Override
    public void add(ItemCat itemCat) {
        itemCatDao.insertSelective(itemCat);
    }

    @Override
    public ItemCat findOne(Long id) {
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(id);
        return itemCat;
    }

    @Override
    public Long delete(Long[] ids) {

//        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
//        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();

        if (ids!=null){
            for (Long id : ids) {
                criteria.andParentIdEqualTo(id);
                List<ItemCat> itemCats = itemCatDao.selectByExample(itemCatQuery);
                if (itemCats.size()>0){
                    return 1l;
                }else {
                    itemCatDao.deleteByPrimaryKey(id);
                    return 0l;
                }

            }

        }
        return 1l;
    }

    @Override
    public PageResult search(Integer page, Integer rows, ItemCat itemCat) {
        //向缓存中加入数据
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        for (ItemCat cat : itemCats) {
            redisTemplate.boundHashOps("item").put(cat.getName(),cat.getTypeId());

        }
        //分割线---------------------------------
        PageHelper.startPage(page,rows);

        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();

        Page<ItemCat> page1 = (Page<ItemCat>) itemCatDao.selectByExample(null);

        return new PageResult(page1.getTotal(),page1.getResult());
    }

    @Override
    public void update(ItemCat itemCat) {
        itemCatDao.updateByPrimaryKeySelective(itemCat);
    }


    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }
}
