package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.ItemCarService;
import cn.itcast.core.service.type_templateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/itemCat")
public class itemCatController {
    @Reference
    private ItemCarService itemCarService;
    @Reference
    private type_templateService type_templateService;
    @RequestMapping("/findByParentId")
    public List<ItemCat>findByParetId(Long parentId){
      return  itemCarService.findByParetId(parentId);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody ItemCat itemCat){

        try {
            itemCarService.add(itemCat);
            return new Result(true,"成了");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"败了");
        }

    }

    @RequestMapping("/findOne")
    public ItemCat findOne(Long id){

          return itemCarService.findOne(id);


    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){

       Long i= itemCarService.delete(ids);

       if (i==0l){
           return new Result(true,"删除成功");
       }else {
           return new Result(false,"有东西");
       }
    }
    @RequestMapping("/selecttypeList")
    public List<Map> selecttypeList(){
        return type_templateService.findAll();
    }

    @RequestMapping("/search")
    public PageResult search(Integer page,Integer rows,@RequestBody ItemCat itemCat){


        return itemCarService.search(page,rows,itemCat);
    }
    @RequestMapping("/update")
    public Result update(@RequestBody ItemCat itemCat){
        try {
        itemCarService.update(itemCat);
            return new Result(true,"修改成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
}
