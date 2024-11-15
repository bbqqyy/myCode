package com.bqy.user_center.service;

import com.bqy.user_center.mapper.UserCenterMapper;
import com.bqy.user_center.model.domain.UserCenter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserCenterService userCenterService;
    //自定义线程池

    private ExecutorService executorService = new ThreadPoolExecutor(60,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
    //CPU密集型：分配的核心线程队列 = CPU核数-1
    //IO密集型分配的核心线程数可以大于CPU核数
    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUMBER = 1000;
        List<UserCenter> list = new ArrayList<>();
        for(int i = 0; i<INSERT_NUMBER;i++){
            UserCenter userCenter = new UserCenter();
            userCenter.setUsername("假用户");
            userCenter.setUserAccount("fakeUser");
            userCenter.setAvatarUrl("");
            userCenter.setUserPassword("12345678");
            userCenter.setGender(0);
            userCenter.setTelephone("123456");
            userCenter.setEmail("88888@qq.com");
            userCenter.setUserStatus(0);
            userCenter.setUserRole(0);
            userCenter.setPlanetCode("111111");
            userCenter.setTags("[]");
            userCenter.setUserProfile("假的数据");
            list.add(userCenter);
        }
        userCenterService.saveBatch(list,100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }
    /**
     * 并发批量插入用户（使用多线程）
     */
    @Test
    public void doConcurrencyInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUMBER = 100000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        //分十组
        int j = 0;
        for(int i = 0; i<10;i++){
            List<UserCenter> list = new ArrayList<>();
            while(true){
                j++;
                UserCenter userCenter = new UserCenter();
                userCenter.setUsername("假用户");
                userCenter.setUserAccount("fakeUser");
                userCenter.setAvatarUrl("");
                userCenter.setUserPassword("12345678");
                userCenter.setGender(0);
                userCenter.setTelephone("123456");
                userCenter.setEmail("88888@qq.com");
                userCenter.setUserStatus(0);
                userCenter.setUserRole(0);
                userCenter.setPlanetCode("111111");
                userCenter.setTags("[]");
                userCenter.setUserProfile("假的数据");
                list.add(userCenter);
                if(j % 10000 == 0){
                    break;
                }

            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
                System.out.println("threadName:"+Thread.currentThread().getName());
                userCenterService.saveBatch(list,10000);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }

}
