package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import entity.SpecificationVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService  {
    @Autowired
    SpecificationDao specificationDao;
    @Autowired
    SpecificationOptionDao specificationOptionDao;
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {


        SpecificationQuery specificationQuery = new SpecificationQuery();

        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();

        if (null!=specification.getSpecName()&&specification.getSpecName().trim()!=""){
            criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
        }
        PageHelper.startPage(page,rows);
       Page<Specification> specifications= (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        return new PageResult(specifications.getTotal(),specifications.getResult());

//        PageHelper.startPage(page,rows);
//
//        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(null);
//
//        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public void add(SpecificationVo specificationVo) {
        specificationDao.insertSelective(specificationVo.getSpecification());
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();

        for (SpecificationOption specificationOption : specificationOptionList) {
            specificationOption.setSpecId(specificationVo.getSpecification().getId());

            specificationOptionDao .insertSelective(specificationOption);
        }

    }

    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo specificationVo = new SpecificationVo();

        Specification specification = specificationDao.selectByPrimaryKey(id);

        specificationVo.setSpecification(specification);

        SpecificationOptionQuery query = new SpecificationOptionQuery();

        query.createCriteria().andSpecIdEqualTo(id);

        specificationVo.setSpecificationOptionList(specificationOptionDao.selectByExample(query));
        return specificationVo;
    }

    @Override
    public void update(SpecificationVo specificationVo) {
        specificationDao.updateByPrimaryKeySelective(specificationVo.getSpecification());

        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(specificationVo.getSpecification().getId());

        specificationOptionDao.deleteByExample(query);

        List<SpecificationOption> list = specificationVo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : list) {
            specificationOptionDao.insertSelective(specificationOption);
        }



    }

    @Override
    public void delete(Long[] ids) {
        if (ids!=null){
            for (Long id : ids) {
                specificationDao.deleteByPrimaryKey(id);
            }
        }

    }

    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
