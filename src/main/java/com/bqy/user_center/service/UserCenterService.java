package com.bqy.user_center.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bqy.user_center.model.domain.UserCenter;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bqy.user_center.model.domain.request.UserSearchRequest;
import com.bqy.user_center.model.domain.request.UserUpdatePasswordRequest;
import com.bqy.user_center.model.domain.vo.UserVo;
import com.bqy.user_center.utils.BaseResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.bqy.user_center.contant.UserContent.ADMIN_ROLE;
import static com.bqy.user_center.contant.UserContent.USER_LOGIN_STATE;

/**
* @author 86173
* @description 针对表【user_center(用户表)】的数据库操作Service
* @createDate 2024-04-21 10:00:09
*/
public interface UserCenterService extends IService<UserCenter> {

    //用户注册

    /**
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param planetCode
     * @return
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return 脱敏后的用户信息
     */
    UserCenter userLogin(String userAccount, String userPassword, HttpServletRequest request);


    UserCenter getSafetyUser(UserCenter originUser);

    int userLogout(HttpServletRequest request);
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    UserCenter getLoginUser(HttpServletRequest request);

    /**
     * 修改密码
     *
     * @param updatePasswordRequest
     * @param request
     */
    boolean updateUserPassword(UserUpdatePasswordRequest updatePasswordRequest, HttpServletRequest request);

    /**
     * 分页条件
     * @param searchRequest
     * @return
     */
    QueryWrapper<UserCenter> getQueryWrapper(UserSearchRequest searchRequest);

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<UserCenter> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param userCenter
     * @return
     */
    int updateUser(UserCenter userCenter,UserCenter loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    UserCenter LoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(UserCenter loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<UserCenter> matchUsers(long num, UserCenter loginUser);
}
