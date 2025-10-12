package com.mik.sys.entity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Table("operation_log")
public class OperationLogEntity extends BaseEntity {
    private Long operationId;
    private Long userId;
    private String operationName;
    private String ip;
    private String url;
    private String param;
    private Integer result;
}
