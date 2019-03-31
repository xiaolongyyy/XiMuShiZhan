package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows,@RequestBody Seller seller){
        PageResult pageResult=sellerService.search(page,rows,seller);
        return pageResult;
    }
    @RequestMapping("/findOne")
    public Seller findOne(String id){
        return sellerService.findOne(id);

    }
    @RequestMapping("/updateStatus")
    public Result updateStatus(String sellerId,String status){
        System.out.println(sellerId);
        System.out.println(status);
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"成功了");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败了");
        }

    }


}
