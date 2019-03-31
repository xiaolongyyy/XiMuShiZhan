package cn.itcast.core.controller;


import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.GoodsServer;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.GoodsVo;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("all")
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsServer goodsServer;

    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVo goodsVo) {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println(name);
            goodsVo.getGoods().setSellerId(name);

            goodsServer.add(goodsVo);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败");
        }

    }

    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods) {
        String name = goodsServer.Cnie(SecurityContextHolder.getContext().getAuthentication().getName());
        System.out.println(name);
        goods.setSellerId(name);
        return goodsServer.search(page, rows, goods);
    }

    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id) {
        return goodsServer.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody GoodsVo goodsVo) {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsVo.getGoods().setSellerId(name);

            goodsServer.update(goodsVo);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }

    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            goodsServer.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }


    }
}
