package com.mik.core.pojo;

import lombok.Data;

@Data
public class PageInput {
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public PageInput() {
    }

    public PageInput(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        if (pageSize < 0) {
            this.pageSize = 10;
        } else if (pageSize > 10000) {
            this.pageSize = 10000;
        } else {
            this.pageSize = pageSize;
        }
    }

    public static PageInput of(Integer pageNum, Integer pageSize) {
        return new PageInput(pageNum, pageSize);
    }
}
