package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCarService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.opensaml.xml.signature.P;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCarService itemCarService;

    @RequestMapping("/findByParentId")
    public List<ItemCat>findBuParentId(Long parentId){
        return itemCarService.findByParetId(parentId);
    }
    @RequestMapping("/findOne")
    public ItemCat findOne(Long id){
       return itemCarService.findOne(id);
    }

    @RequestMapping("/findAll")
    public List<ItemCat>findAll(){
        return itemCarService.findAll();
    }
}
