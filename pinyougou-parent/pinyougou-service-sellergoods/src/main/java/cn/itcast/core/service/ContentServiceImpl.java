package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private ContentDao contentDao;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		contentDao.insertSelective(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	//修改的操作
	@Override
	public void edit(Content content) {
		//从数据库查询看是否改变属性
		Content c = contentDao.selectByPrimaryKey(content.getId());
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());

		if (!c.getCategoryId().equals(content.getCategoryId())){
			redisTemplate.boundHashOps("content").delete(c.getCategoryId());
		}

		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				//执行删除操作需要获取你存储的Id
				Content content = contentDao.selectByPrimaryKey(id);
				contentDao.deleteByPrimaryKey(id);
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
			}
		}
	}
	//轮播图效果展示<<<<<<<<<<<<<<
	//引入所需要的依赖最好作用在dao层
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
		//使用缓存的方式储存轮播图
		List<Content> contents= (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		if (contents==null||contents.size()==0){
	    //根据条件查询
        ContentQuery contentQuery = new ContentQuery();

        ContentQuery.Criteria criteria = contentQuery.createCriteria();
        //条件为id为轮播图id 且状态为1的
        criteria.andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
        //条件升序降序排列
        contentQuery.setOrderByClause("sort_order desc");

        contents=contentDao.selectByExample(contentQuery);
		redisTemplate.boundHashOps("content").put(categoryId,contents);
		}
		return contents;

    }

}
