package com.mik.sys;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    // 操作名称，如“创建用户”
    String operation() default "";

    boolean paramRecord() default true;
}
