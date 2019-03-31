package cn.itcast.core.service;

import java.util.Map;

public interface ItemSearchService {
    Map<String,Object> search(Map<String,String> searchMap);
    Map<String,Object> search1(Map<String,String> searchMap);
    Map<String,Object> search3(Map<String,String> searchMap);
}
