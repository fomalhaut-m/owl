package com.vex.owl.queryer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页请求
 * <p>
 * 用于指定查询结果的分页信息
 * <p>
 * 示例：
 * <pre>
 *     QueryerPageable.of(0, 20)  // 第1页，每页20条
 *     QueryerPageable.first()     // 第1页，每页20条
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryerPageable {
    /**
     * 页码，从 0 开始
     */
    private int num = 0;

    /**
     * 每页条数，默认 20
     */
    private int size = 20;

    /**
     * 创建分页对象
     *
     * @param num  页码，从 0 开始（0 表示第一页）
     * @param size 每页条数
     * @return QueryerPageable 实例
     */
    public static QueryerPageable of(int num, int size) {
        return new QueryerPageable(num, size);
    }

}
