package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import entity.Result;
import entity.SpecificationVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    SpecificationService specificationService;
    @RequestMapping("/search")
    //public PageResult search(Integer pageNum, Integer pageSize,@RequestBody(required = false) Brand brand){
    public PageResult search(Integer page, Integer rows,@RequestBody Specification specification){
        System.out.println("wojinali");
        return specificationService.search(page,rows,specification);
    }


    @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.add(specificationVo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
    @RequestMapping("/findOne")
    public SpecificationVo findOne(Long id){
        return specificationService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.update(specificationVo);
            return new Result(true,"牛逼啊");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"拉进");
        }


    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"欧了");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"豆豆");
        }

    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return specificationService.selectOptionList();


    }

}
