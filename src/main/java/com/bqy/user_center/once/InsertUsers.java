package com.bqy.user_center.once;
import java.util.Date;

import com.bqy.user_center.mapper.UserCenterMapper;
import com.bqy.user_center.model.domain.UserCenter;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UserCenterMapper userCenterMapper;

    /**
     * 批量插入用户
     */
//    @Scheduled
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUMBER = 10000000;
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
            userCenterMapper.insert(userCenter);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }
}
