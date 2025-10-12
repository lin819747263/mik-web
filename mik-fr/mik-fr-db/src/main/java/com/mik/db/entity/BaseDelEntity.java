package com.mik.db.entity;

import com.mybatisflex.annotation.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public class BaseDelEntity {

    @Column(onInsertValue = "now()")
    private Date createTime;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private Date updateTime;

    @Column(isLogicDelete = true)
    private Integer deleted;

}
