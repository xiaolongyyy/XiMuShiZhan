package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerDao sellerDao;
    @Override
    public void add(Seller seller) {
        seller.setStatus("0");
        sellerDao.insertSelective(seller);
    }
//页面回显操作
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page,rows);

        SellerQuery sellerQuery = new SellerQuery();

        SellerQuery.Criteria criteria = sellerQuery.createCriteria();

        if (seller.getName()!=null &&!"".equals(seller.getName().trim())){
             criteria.andNameEqualTo("%"+seller.getName()+"%");
        }
        if (seller.getNickName()!=null&&!"".equals(seller.getNickName().trim())){
            criteria.andNickNameEqualTo("%"+seller.getNickName()+"%");
        }
        Page<Seller> sellers = (Page<Seller>) sellerDao.selectByExample(sellerQuery);
        return new PageResult(sellers.getTotal(),sellers.getResult());


    }

    @Override
    public Seller findOne(String id) {
        Seller seller = sellerDao.selectByPrimaryKey(id);
        return seller;

    }

    @Override
    public void updateStatus(String selllerId, String status) {
        Seller seller = new Seller();

        seller.setSellerId(selllerId);
        seller.setStatus(status);

        sellerDao.updateByPrimaryKeySelective(seller);

    }
}
