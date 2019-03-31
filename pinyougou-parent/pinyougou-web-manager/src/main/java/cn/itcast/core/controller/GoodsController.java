package cn.itcast.core.controller;


import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsServer;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.GoodsVo;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("all")
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsServer goodsServer;



    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods) {
        //String name = goodsServer.Cnie(SecurityContextHolder.getContext().getAuthentication().getName());
        //System.out.println(name);
       // goods.setSellerId(name);
        return goodsServer.search(page, rows, goods);
    }
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsServer.updateStatus(ids,status);
            return new Result(true,"审核通过");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"程序错误");
        }

    }
    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id) {
        return goodsServer.findOne(id);
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
