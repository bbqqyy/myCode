package com.bqy.user_center.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户表
 * @TableName user_center
 */
@TableName(value ="user_center")
@Data
public class UserCenter implements Serializable {
    /**
     * 用户ID
     */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String telephone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态 0 正常
     */
    private Integer userStatus;
    /**
     * 用户角色
     * 0 普通用户
     * 1 管理员
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 更新时间
     */
    private Date updatetime;

    /**
     * 是否被删除 0未被删除
     */
    @TableLogic
    private Integer isDelete;
    /**
     * 星球编号
     */
    private String planetCode;
    /**
     * 标签列表 json格式
     */
    private String tags;
    /**
     * 个人简介
     */
    private String userProfile;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}