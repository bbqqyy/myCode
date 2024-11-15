package com.bqy.user_center.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除
 */
@Data
public class UserDeleteRequest implements Serializable {

    /**
     * id
     */

    private Long id;

    private static final long serialVersionUID = 1L;
}
