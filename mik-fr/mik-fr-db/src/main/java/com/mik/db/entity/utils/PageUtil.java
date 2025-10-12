package com.mik.db.entity.utils;

import com.mik.core.pojo.PageResult;
import com.mybatisflex.core.paginate.Page;

public class PageUtil {

    public static <T> PageResult<T> transform(Page<T> page){
        return new PageResult(page.getTotalRow(), page.getRecords());
    }
}
