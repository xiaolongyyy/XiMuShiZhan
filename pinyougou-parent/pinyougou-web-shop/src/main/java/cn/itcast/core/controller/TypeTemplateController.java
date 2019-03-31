package cn.itcast.core.controller;


import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.type_templateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private type_templateService type_templateService;

    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id){
       return type_templateService.findOne(id);

    }
    @RequestMapping("/findBySpecList")
    public List<Map>findSpecList(Long id){
        return type_templateService.findSpecList(id);
    }




}
