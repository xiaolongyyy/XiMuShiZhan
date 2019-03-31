package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.type_templateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.plaf.PanelUI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class type_templateController {
    @Reference
    type_templateService type_templateService;

    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate) {

        return type_templateService.search(page, rows, typeTemplate);


    }

    @RequestMapping("/add")
    public Result add(@RequestBody TypeTemplate typeTemplate) {

        try {
            type_templateService.add(typeTemplate);
            return new Result(true, "成了");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }


    }

    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id) {
        return type_templateService.findOne(id);


    }

    @RequestMapping("/update")
    public Result update(@RequestBody TypeTemplate typeTemplate) {
        try {
            type_templateService.update(typeTemplate);
            return new Result(true, "成了");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "凉凉");
        }

    }

    @RequestMapping("/dele")
    public Result dele(Long[] ids) {

        try {
            type_templateService.dele(ids);
            return new Result(true, "成了");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "凉凉");
        }

    }


}