package com.bqy.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.user_center.exception.BusinessException;
import com.bqy.user_center.model.domain.request.*;
import com.bqy.user_center.model.domain.vo.UserVo;
import com.bqy.user_center.utils.BaseResponse;
import com.bqy.user_center.model.domain.UserCenter;
import com.bqy.user_center.service.UserCenterService;
import com.bqy.user_center.utils.ErrorCode;
import com.bqy.user_center.utils.ResultUtils;
import com.bqy.user_center.utils.ThrowUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.bqy.user_center.contant.UserContent.ADMIN_ROLE;
import static com.bqy.user_center.contant.UserContent.USER_LOGIN_STATE;

/*
用户接口
@author:bqy
@RestController注解适用于编写restful风格的api返回值默认为json格式类型

 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理模块")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserCenterController {

    @Resource
    private UserCenterService userService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserCenter> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<UserCenter> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserCenter currentUser = (UserCenter) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        UserCenter user = userService.getById(userId);
        UserCenter safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        UserCenter user = new UserCenter();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 查询用户
     *
     * @param searchRequest
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<UserCenter>> searchUsers(UserSearchRequest searchRequest, HttpServletRequest request) {
        // 管理员校验
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }

        QueryWrapper<UserCenter> queryWrapper = userService.getQueryWrapper(searchRequest);
        List<UserCenter> userList = userService.list(queryWrapper);
        List<UserCenter> users = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
        return ResultUtils.success(users);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest deleteRequest, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long id = deleteRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean removeUser = userService.removeById(id);
        if (!removeUser) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {

        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter user = new UserCenter();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户自己更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userService.getLoginUser(request);
        UserCenter user = new UserCenter();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 修改密码
     *
     * @param updatePasswordRequest
     * @param request
     * @return
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody UserUpdatePasswordRequest updatePasswordRequest,
                                                    HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        boolean updateUserPassword = userService.updateUserPassword(updatePasswordRequest, request);
        if (updateUserPassword) {
            return ResultUtils.success(true);
        } else {
            return ResultUtils.error(ErrorCode.INVALID_PASSWORD_ERROR);
        }
    }


    /**
     * 搜索标签
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<UserCenter>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<UserCenter> userCenterList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userCenterList);
    }

    /**
     * 更新用户信息
     *
     * @param userCenter
     * @return
     */
    @PostMapping("/update/user")
    public BaseResponse<Integer> updateUser(@RequestBody UserCenter userCenter, HttpServletRequest request) {
        //判断参数是否为空
        if (userCenter == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        UserCenter user = userService.LoginUser(request);
        Integer result = userService.updateUser(userCenter, user);
        return ResultUtils.success(result);


    }

    @GetMapping("/recommend")
    public BaseResponse<Page<UserCenter>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {

        UserCenter user = userService.LoginUser(request);
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        //如果有缓存直接读缓存
        String redisKey = String.format("bqy:user:recommend:%s", user.getId());
        Page<UserCenter> userPage = (Page<UserCenter>) operations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        //无缓存
        QueryWrapper<UserCenter> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id",user.getId());
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //写缓存
        try {
            operations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    @GetMapping("/match")
    public BaseResponse<List<UserCenter>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCenter loginUser = userService.LoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));

    }


}
