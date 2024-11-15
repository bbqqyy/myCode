package com.bqy.user_center.service;
import java.util.Date;

import com.bqy.user_center.model.domain.UserCenter;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增加
        valueOperations.set("bbqqString","yyds");
        valueOperations.set("bbqqInt",1);
        valueOperations.set("bbqqBoolean",true);
        UserCenter user = new UserCenter();
        user.setId(1L);
        user.setUsername("好兄弟");
        valueOperations.set("bbqqUser",user);
        //查
        Object bbqq = valueOperations.get("bbqqString");
        Assertions.assertTrue("yyds".equals((String) bbqq));
        bbqq = valueOperations.get("bbqqInt");
        Assertions.assertTrue(1==(Integer)bbqq); // 这里不需要类型转换，直接比较即可
        bbqq = valueOperations.get("bbqqBoolean");
        Assertions.assertTrue((Boolean)bbqq);
        valueOperations.set("bbqqString","yy");
        Assertions.assertEquals(valueOperations.get("bbqqString"),"yy");
        redisTemplate.delete("bbqqInt");
    }
}
