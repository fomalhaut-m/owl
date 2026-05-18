package com.vex.owl.queryer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序
 * <p>
 * 用于指定查询结果的排序方式
 * <p>
 * 示例：
 * <pre>
 *     QueryerSort.asc("createTime")
 *     QueryerSort.desc("username")
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryerSort {
    /**
     * 排序字段
     */
    private String property;

    /**
     * 排序方向，默认 ASC
     */
    private String direction = "ASC";

    /**
     * 判断是否为降序排序
     *
     * @return true 表示降序（DESC），false 表示升序（ASC）
     */
    public boolean isDesc() {
        return "DESC".equals(direction);
    }

    /**
     * 创建默认排序规则（升序）
     *
     * @param property 排序字段名
     * @return QueryerSort 实例，默认按升序排列
     */
    public static QueryerSort of(String property) {
        QueryerSort s = new QueryerSort();
        s.property = property;
        s.direction = "ASC";
        return s;
    }

    /**
     * 创建自定义排序规则
     *
     * @param property  排序字段名
     * @param direction 排序方向，"ASC" 表示升序，"DESC" 表示降序
     * @return QueryerSort 实例
     */
    public static QueryerSort of(String property, String direction) {
        QueryerSort s = new QueryerSort();
        s.property = property;
        s.direction = direction;
        return s;
    }

    /**
     * 创建升序排序规则
     *
     * @param property 排序字段名
     * @return QueryerSort 实例，按升序排列
     */
    public static QueryerSort asc(String property) {
        return of(property, "ASC");
    }

    /**
     * 创建降序排序规则
     *
     * @param property 排序字段名
     * @return QueryerSort 实例，按降序排列
     */
    public static QueryerSort desc(String property) {
        return of(property, "DESC");
    }
}
