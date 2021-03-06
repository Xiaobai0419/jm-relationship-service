package com.sunfield.microframe.common.utils;

import com.sunfield.microframe.common.response.Page;

import java.util.ArrayList;
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
        //list.subList(fromIndex, toIndex)将无法序列化，因为从subList()返回的子列表对象未实现无参构造函数。
        return new Page<>(list.size(),pageSize,pageNumber,new ArrayList<>(list.subList(fromIndex, toIndex)));
    }
}
