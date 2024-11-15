package com.bqy.user_center.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户队伍关系
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 用户id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /**
     * 队伍id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long teamId;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}