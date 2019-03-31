package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@SuppressWarnings("all")
@Service
public class ItemsearchServerImpl implements ItemSearchService{
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    //进行普通查询只进行简单查询

    @Override
    public Map<String, Object> search(Map<String,String> searchMap) {
        //创建一个返回集的集合
        Map<String, Object> map = new HashMap<>();
        //查询商品分类的结果集
        List<String> categoryList = searchCategoryListByKeywords(searchMap);
        map.put("categoryList",categoryList);
        if (categoryList.size()>0&&null!=categoryList){
            map.putAll(searchBrandListAndSpecListByCategory(categoryList.get(0)));
        }
        map.putAll(search3(searchMap));

        return map ;
    }
    //成品能执行查询以及分类分页高亮
    public Map<String, Object> search1_1(Map<String,String> searchMap) {
        //创建一个返回集的集合
        Map<String, Object> map = new HashMap<>();
        //查询商品分类的结果集
        List<String> categoryList = searchCategoryListByKeywords(searchMap);
        map.put("categoryList",categoryList);
        if (categoryList.size()>0&&null!=categoryList){
            map.putAll(searchBrandListAndSpecListByCategory(categoryList.get(0)));
        }
        map.putAll(search3(searchMap));

        return map ;
    }
    //查询规格
    public Map<String,Object>searchBrandListAndSpecListByCategory(String category){
        Map<String, Object> resultMap = new HashMap<>();

        //通过你的分类id查询模板
        Object typeId = redisTemplate.boundHashOps("item").get(category);
        //查询通过模板id的结果集
        System.out.println(typeId);
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        //通过模板ID查询结果集
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        //向集合种添加数据
        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);

        return resultMap;

    }
    //从分类中查询商品
    public List<String> searchCategoryListByKeywords(Map<String,String> searchMap){
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query simpleQuery = new SimpleQuery(criteria);
        //创建一个分组的域
        GroupOptions groupOptions = new GroupOptions();
        //向域中添加分组方式
        groupOptions.addGroupByField("item_category");
        //分组方式添加到条件种
        simpleQuery.setGroupOptions(groupOptions);

        //查询商品分类 分组查询
        GroupPage<Item> page = solrTemplate.queryForGroupPage(simpleQuery, Item.class);
        //创建一个储存的集合 储存为字符串的类型
        ArrayList<String> categoryList = new ArrayList<>();

        //获取page分组 获取需要的属性
        GroupResult<Item> item_category = page.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        //判断content是否为空
        if (content.size()>0&&null!=content){
            for (GroupEntry<Item> itemGroupEntry : content) {
                categoryList.add(itemGroupEntry.getGroupValue());
            }
        }
        return categoryList;

    }
    @Override
    public Map<String,Object> search1(Map searchMap) {

        Map<String, Object> map = new HashMap<>();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query simpleQuery = new SimpleQuery(criteria);

        ScoredPage<Item> maps = solrTemplate.queryForPage(simpleQuery, Item.class);

        map.put("rows",maps.getContent());
        return map;
    }

    //进行分页查询
    public Map<String,Object> search2(Map searchMap) {

        Map<String, Object> map = new HashMap<>();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query simpleQuery = new SimpleQuery(criteria);

        //提取从页面中接收的页数及每页显示的
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        simpleQuery.setOffset((pageNo-1)*pageSize);
        simpleQuery.setRows(pageSize);
        //执行查询像集合中添加数据
        ScoredPage<Item> maps = solrTemplate.queryForPage(simpleQuery, Item.class);
        //设置总条数
        map.put("total",maps.getTotalElements());
        //设置总页数 为多少在页面上显示
        map.put("totalPages",maps.getTotalPages());
        //显示页面加载数据
        map.put("rows",maps.getContent());
        return map;
    }
    //高亮分页基本数据显示
    @Override
    public Map<String,Object> search3(Map searchMap) {
        //创建返回值的集合
        Map<String, Object> map = new HashMap<>();
        //设置需要查询的条件
        //去掉你的搜索条件中的空格
        String keywords = (String) searchMap.get("keywords");
        //三星 手机
        keywords=keywords.replace(" ","");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //Query simpleQuery = new SimpleQuery(criteria);
        HighlightQuery simpleQuery = new SimpleHighlightQuery();
         simpleQuery.addCriteria(criteria);
        //设置高亮的域
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置高亮的字段及样式
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //把条件加入条件中
        simpleQuery.setHighlightOptions(highlightOptions);

        //课后
        //关键字查询
        //按商品过滤查询
        if (!"".equals(searchMap.get("category"))){
            //创建一个查询商品的条件加入查询条件中
            FilterQuery simpleFilterQuery = new SimpleFilterQuery();
            //设置根据什么查询
            Criteria criteria_category = new Criteria("item_category").is(searchMap.get("category"));
            simpleFilterQuery.addCriteria(criteria_category);

            simpleQuery.addFilterQuery(simpleFilterQuery);
        }
        //设置品牌分类的代码  代码同上部差不多
        if (!"".equals(searchMap.get("brand"))){
            FilterQuery simpleFilterQuery = new SimpleFilterQuery();
            Criteria criteria_brand = new Criteria("item_brand").is(searchMap.get("brand"));
            simpleFilterQuery.addCriteria(criteria_brand);
            simpleQuery.addFilterQuery(simpleFilterQuery);
        }

        //设置规格查询的条件
        if (null!=searchMap.get("spec")){
            //获取规格中的数据
            Map<String,String> spec = (Map<String, String>) searchMap.get("spec");
            //遍历查询规格
            for (String key : spec.keySet()) {
                Criteria item_title = new Criteria("item_spec_"+key).is(spec.get(key));
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();

                simpleFilterQuery.addCriteria(item_title);

                simpleQuery.addFilterQuery(simpleFilterQuery);
            }

        }
        //设置新品




        //按照价格分组
        if (!"".equals(searchMap.get("price"))){
            //拿出价格的前后并进行分割成数组
            String price = (String) searchMap.get("price");
            String[] split = price.split("-");

            //判断第一个数据合适的
            if (!split[0].equals("0")){
                Criteria item_price = new Criteria("item_price").greaterThanEqual(split[0]);
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                simpleFilterQuery.addCriteria(item_price);
                simpleQuery.addFilterQuery(simpleFilterQuery);
            }
            //判断第二个数据合适的
            if (!split[1].equals("大炮")){
                Criteria item_price = new Criteria("item_price").lessThanEqual(split[1]);
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                simpleFilterQuery.addCriteria(item_price);
                simpleQuery.addFilterQuery(simpleFilterQuery);
            }
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();

        }
        //排序升降的顺序
        String sort = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        //判断是否进行了排序操作
        if (sort!=null&&!"".equals(sort)){
            if (sort.equals("ASC")){
                //创建排序的条件
                Sort sortASC = new Sort(Sort.Direction.ASC,"item_"+sortField);
                simpleQuery.addSort(sortASC);
            }
            if (sort.equals("DESC")){
                Sort sortDESC = new Sort(Sort.Direction.DESC,"item_"+sortField);
                simpleQuery.addSort(sortDESC);
            }
        }



        //提取从页面中接收的页数及每页显示的
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        simpleQuery.setOffset((pageNo-1)*pageSize);
        simpleQuery.setRows(pageSize);
        //执行查询像集合中添加数据
        HighlightPage<Item> maps = solrTemplate.queryForHighlightPage(simpleQuery, Item.class);
        //查询出数据库中的高亮显示
        //进行遍历让后存入你的页面数据中覆盖
        List<HighlightEntry<Item>> highlighted = maps.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //拿出原本返回的数据
            Item entity = itemHighlightEntry.getEntity();
            //查询高亮显示的数据
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            //判读数据是否为空如果为空回报空指针
            if (null!=highlights&&highlights.size()>0){
              //覆盖数据
            entity.setTitle(highlights.get(0).getSnipplets().get(0));

            }

        }
        //设置总条数
        map.put("total",maps.getTotalElements());
        //设置总页数 为多少在页面上显示
        map.put("totalPages",maps.getTotalPages());
        //显示页面加载数据
        map.put("rows",maps.getContent());
        return map;
    }

    public Map<String,Object> search3_1(Map searchMap) {
        //创建返回值的集合
        Map<String, Object> map = new HashMap<>();
        //设置需要查询的条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //Query simpleQuery = new SimpleQuery(criteria);
        HighlightQuery simpleQuery = new SimpleHighlightQuery(criteria);

        //设置高亮的域
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置高亮的字段及样式
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //把条件加入条件中
        simpleQuery.setHighlightOptions(highlightOptions);


        //提取从页面中接收的页数及每页显示的
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        simpleQuery.setOffset((pageNo-1)*pageSize);
        simpleQuery.setRows(pageSize);
        //执行查询像集合中添加数据
        HighlightPage<Item> maps = solrTemplate.queryForHighlightPage(simpleQuery, Item.class);
        //查询出数据库中的高亮显示
        //进行遍历让后存入你的页面数据中覆盖
        List<HighlightEntry<Item>> highlighted = maps.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //拿出原本返回的数据
            Item entity = itemHighlightEntry.getEntity();
            //查询高亮显示的数据
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            //判读数据是否为空如果为空回报空指针
            if (null!=highlights&&highlights.size()>0){
                //覆盖数据
                entity.setTitle(highlights.get(0).getSnipplets().get(0));

            }

        }
        //设置总条数
        map.put("total",maps.getTotalElements());
        //设置总页数 为多少在页面上显示
        map.put("totalPages",maps.getTotalPages());
        //显示页面加载数据
        map.put("rows",maps.getContent());
        return map;
    }



}
