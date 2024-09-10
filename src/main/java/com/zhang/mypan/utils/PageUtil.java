package com.zhang.mypan.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Arrays;
import java.util.List;

public class PageUtil {
    public static <T> Page<T> page(List<T> list, Integer pageNo, Integer pageSize) {
        if (list == null || list.isEmpty()) {
            return new Page<>();
        }

        // 创建 Page 对象
        Page<T> page = new Page<>(pageNo, pageSize);
        page.setRecords(list);

        // 计算总条目数
        page.setTotal(list.size());

        // 计算当前页的起始和结束位置
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, list.size());

        int pages = list.size() / pageSize;
        // 创建当前页的数据子列表
        List<T> pageList = list.subList(fromIndex, toIndex);
        // 将子列表设置到 Page 对象中
        page.setRecords(pageList);
        page.setCurrent(pageNo);
        page.setPages(pages);

        return page;
    }

    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

        final Page<Integer> page = page(list, 3, 5);
        System.out.println(page.getPages()); // 页数
        System.out.println(page.getCurrent()); // 当前页
        System.out.println(page.getSize()); // 每页大小
        System.out.println(page.getRecords()); // 当前页数据
        System.out.println(page.getTotal()); // 数据总数
    }

}
