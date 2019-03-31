package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.service.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@SuppressWarnings("all")
//@Service
public class ItemSearchServerImpl2 implements ItemSearchService{
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    //只编写调用
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        //进来先处理关键字
        String replace = (String) searchMap.get("keywords");
        replace=replace.replaceAll(" ","");
        resultMap.put("keywords",replace);
        //创建条件信息
        //获取分类信息
        List<String> categoryList =searchCategoryListByKeywords(resultMap);
        resultMap.put("categoryList",categoryList);
        //获取规格以及品牌
        //如果你的分类下没有商品则不执行
        if (categoryList!=null&&categoryList.size()>0) {
            Map<String, Object> stringObjectMap = searchBrandListAndSpecListByCategory(categoryList.get(0));
        }
        Map<String,Object>search=searchG(searchMap);
        resultMap.putAll(search);
        return resultMap;
    }

    //高亮 分页  过滤等
    public Map<String, Object> searchG(Map<String,String>search) {
        Map<String, Object> searchMap = new HashMap<>();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //SimpleQuery simpleQuery = new SimpleQuery(criteria);
        //设置高亮的查询条件
        HighlightQuery simpleHighlightQuery = new SimpleHighlightQuery(criteria);
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        simpleHighlightQuery.setHighlightOptions(highlightOptions);

        //设置分页
        Integer pageNo = Integer.parseInt(search.get("pageNo"));
        Integer pageSize = Integer.parseInt(search.get("pageSize"));
        simpleHighlightQuery.setOffset((pageNo-1)*pageSize);
        simpleHighlightQuery.setRows(pageSize);

        //设置过滤
        //分类过滤
        if (search.get("category")!=null&&search.get("category").equals("")){
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
            Criteria CategoryCriteria = new Criteria("item_category").is(search.get("category"));
            simpleFilterQuery.addCriteria(CategoryCriteria);
            simpleHighlightQuery.addFilterQuery(simpleFilterQuery);

        }
        //品牌过滤
        if (search.get("brand")!=null&&search.get("brand").equals("")){
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
            Criteria CategoryCriteria = new Criteria("item_brand").is(search.get("brand"));
            simpleFilterQuery.addCriteria(CategoryCriteria);
            simpleHighlightQuery.addFilterQuery(simpleFilterQuery);
        }
        //规格进行过滤
        if (search.get("spec")!=null&&search.get("spec").length()>0){
            //必须指定map的类型要不无法遍历
            Map<String,String> spec = JSON.parseObject(search.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entries = spec.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                Criteria SpecCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                simpleFilterQuery.addCriteria(SpecCriteria);
                simpleHighlightQuery.addFilterQuery(simpleFilterQuery);
            }
        }
        //按照价格分组
        if (null!=searchMap.get("price")&&searchMap.get("price").equals("")){
            String price = (String) searchMap.get("price");
            String[] p = price.split("-");
            FilterQuery filterQuery = new SimpleFilterQuery();
            if(price.contains("*")){
                //包含*
                filterQuery.addCriteria(new Criteria("item_price").greaterThanEqual(p[0]));
            }else{
                //不包含*
                filterQuery.addCriteria(new Criteria("item_price").between(p[0],p[1],true,true));

            }
            simpleHighlightQuery.addFilterQuery(filterQuery);
        }
        //排序
        if(null != searchMap.get("sort") && !"".equals(searchMap.get("sort"))){

            //判断是升还是降
            if("ASC".equals(searchMap.get("sort"))){
                //Ctrl + D
                simpleHighlightQuery.addSort(new Sort(Sort.Direction.ASC,"item_" + searchMap.get("sortField")));
            }else{
                simpleHighlightQuery.addSort(new Sort(Sort.Direction.DESC,"item_" + searchMap.get("sortField")));

            }
        }
        //设置高亮的数据
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(simpleHighlightQuery, Item.class);
        //获取高亮的结果集
        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> mapHighlightEntry : highlighted) {
           Item entity = mapHighlightEntry.getEntity();
            List<HighlightEntry.Highlight> highlights = mapHighlightEntry.getHighlights();

            if (null!=highlights&&highlights.size()>0){
                entity.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }
        //分页后的结果集   默认10条  普通结果集
        searchMap.put("rows",page.getContent());
        //总条数
        searchMap.put("total",page.getTotalElements());
        //总页数
        searchMap.put("totalPages",page.getTotalPages());

        return searchMap;


    }

    //查询规格品牌
    public Map<String, Object> searchBrandListAndSpecListByCategory(String category) {
        Map<String, Object> SearchMap = new HashMap<>();
        Object item = redisTemplate.boundHashOps("item").get(category);

        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(item);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(item);

        SearchMap.put("brandList",brandList);
        SearchMap.put("specList",specList);

        return SearchMap;
    }

    //查询商品分类
    public List<String> searchCategoryListByKeywords(Map<String, Object > resultMap) {
        Criteria criteria = new Criteria("item_keywords").is(resultMap.get("keywords"));
        SimpleQuery simpleQuery = new SimpleQuery(criteria);
        GroupOptions groupOptions = new GroupOptions();
        //创建分组信息
        groupOptions.addGroupByField("item_category");
        simpleQuery.setGroupOptions(groupOptions);

        //查询分类加入返回的集合中
        List<String> categoryList = new ArrayList<>();

        GroupPage<Item> page = solrTemplate.queryForGroupPage(simpleQuery, Item.class);
        //分层多page
        GroupResult<Item> item_category = page.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        if (content!=null&&content.size()>0){
            for (GroupEntry<Item> itemGroupEntry : content) {
                String groupValue = itemGroupEntry.getGroupValue();
                categoryList.add(groupValue);
            }
        }
        return categoryList;
    }

    @Override
    public Map<String, Object> search1(Map<String, String> searchMap) {
        return null;
    }

    @Override
    public Map<String, Object> search3(Map<String, String> searchMap) {
        return null;
    }
}
