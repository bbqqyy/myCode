package com.bqy.user_center.service;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){
        List<String> list = new ArrayList<>();
        list.add("bqy");
        System.out.println("list:"+list.get(0));
        list.remove(0);

        RList<String> rList = redissonClient.getList("test-list");
//        rList.add("bqy");
        System.out.println("rlist:"+rList.get(0));
        rList.remove(0);

        Map<String,Integer> map = new HashMap<>();
        map.put("bqy",666);
        System.out.println(map.get("bqy"));

        RMap<String,Integer> rMap = redissonClient.getMap("test-map");
        rMap.put("bqy",666);
        System.out.println(rMap.get("bqy"));
    }
}
