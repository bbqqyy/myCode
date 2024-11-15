package com.bqy.user_center.model.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户退出队伍请求体
 */
@Data
public class TeamQuitRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3772099065191376621L;

    /**
     * 队伍id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long teamId;
}
