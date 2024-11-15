package com.bqy.user_center.model.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4812376240067710230L;
    /**
     * 页面大小
     */
    protected int pageSize = 10;
    /**
     * 当前第几页
     */
    protected int pageNum = 1;
}
