package com.bqy.user_center.service;

import com.bqy.user_center.model.domain.UserCenter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
class UserCenterServiceTest {
    @Resource
    private UserCenterService userCenterService;
    @Test
    public void searchUsersByTags() {
        List<String> tagList = Arrays.asList("java","python");
        List<UserCenter> userList =  userCenterService.searchUsersByTags(tagList);
        Assertions.assertNotNull(userList);
    }

    @Test
    void userRegister() {
    }

    @Test
    void userLogin() {
    }

    @Test
    void getSafetyUser() {
    }

    @Test
    void userLogout() {
    }


    @Test
    void getLoginUser() {
    }

    @Test
    void getQueryWrapper() {
    }

    @Test
    void updateUserPassword() {
    }
}