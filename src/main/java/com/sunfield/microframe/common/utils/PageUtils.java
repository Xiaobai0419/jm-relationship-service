package com.sunfield.microframe.common.utils;

import com.sunfield.microframe.common.response.Page;

import java.util.List;

public class PageUtils {
    /**
     * 应用层分页--静态方法支持泛型
     * @param list
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public static <T> Page<T> pageList(List<T> list, int pageNumber, int pageSize) {
        int total = list.size();
        int fromIndex = (pageNumber - 1) * pageSize;
        if (fromIndex >= total) {
            return new Page<>(total,pageSize,pageNumber);//解决空页中分页信息错误的bug
        }
        if(fromIndex < 0){
            return new Page<>(total,pageSize,pageNumber);
        }
        int toIndex = pageNumber * pageSize;
        if (toIndex > total) {
            toIndex = total;
        }
        return new Page<>(list.size(),pageSize,pageNumber,list.subList(fromIndex, toIndex));
    }
}
