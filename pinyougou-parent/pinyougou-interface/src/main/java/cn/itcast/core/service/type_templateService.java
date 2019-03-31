package cn.itcast.core.service;

import cn.itcast.core.pojo.template.TypeTemplate;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface type_templateService {
    PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);


    void add(TypeTemplate typeTemplate);

    TypeTemplate findOne(Long id);

    void update(TypeTemplate typeTemplate);

    void dele(Long[] ids);

    List<Map> findAll();

    public List<Map> findSpecList(Long id);

}
