package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
public class type_templateServiceImpl implements type_templateService {


    @Autowired
    private TypeTemplateDao typeTemplateDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //添加缓存
        List<TypeTemplate> typeTemplates1 = typeTemplateDao.selectByExample(null);
        for (TypeTemplate template : typeTemplates1) {
            //添加缓存
            //缓存有两种
            List<Map> brandList = JSON.parseArray(template.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);

            List<Map> specList = findSpecList(template.getId());
            redisTemplate.boundHashOps("specList").put(template.getId(),specList);
        }


        PageHelper.startPage(page,rows);

        Page<TypeTemplate> typeTemplates = (Page<TypeTemplate>) typeTemplateDao.selectByExample(null);

        return new PageResult(typeTemplates.getTotal(),typeTemplates.getResult());


    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);

    }

    @Override
    public TypeTemplate findOne(Long id) {
        TypeTemplate ty = typeTemplateDao.selectByPrimaryKey(id);
        return ty;

    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void dele(Long[] ids) {
        if (ids!=null){
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map> findAll() {
        return typeTemplateDao.selectfindAll();

    }

    //新添加商品的规格回显
    @Override
    public List<Map> findSpecList(Long id) {
        //查询模板
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        //因为从数据库中查出的是joso格式的字符串所用要转换成集合
//        因为返回的需要是数据
        List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

        for (Map map : list) {
            //查询规格选项列表
            SpecificationOptionQuery query = new SpecificationOptionQuery();

            query.createCriteria().andSpecIdEqualTo((long)(Integer)map.get("id"));
            map.put("options",specificationOptionDao.selectByExample(query));


        }
        return list;
    }


}
