package com.bqy.user_center.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class DeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6705040908627232505L;

    private long id;
}
