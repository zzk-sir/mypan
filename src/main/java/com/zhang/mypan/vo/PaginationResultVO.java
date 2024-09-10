package com.zhang.mypan.vo;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaginationResultVO<T> {
    private Long totalCount;
    private Long pageSize;
    private Long pageNo;
    private Long pageTotal;
    private List<T> list = new ArrayList<>();

    public PaginationResultVO() {
    }

    public PaginationResultVO(Long totalCount, Long pageSize, Long pageNo, Long pageTotal, List<T> list) {
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        if (pageNo == 0L) pageNo = 1L;
        this.pageNo = pageNo;
        this.pageTotal = pageTotal;
        this.list = list;
    }

    public <S> PaginationResultVO<S> convertPaginationResultVO(Class<S> classz) {
        PaginationResultVO<S> res = new PaginationResultVO<>();
        res.setList(BeanUtil.copyToList(this.list, classz));
        res.setPageNo(this.pageNo);
        res.setPageSize(this.pageSize);
        res.setPageTotal(this.pageTotal);
        res.setPageNo(this.pageNo);
        return res;
    }
}
