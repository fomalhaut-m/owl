package com.vex.owl.queryer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 谓词（条件组合）
 * <p>
 * 用于组合多个条件表达式，支持 AND、OR、NOT 三种逻辑运算
 * <p>
 * 示例：
 * <pre>
 *     QueryerPredicate.and(
 *         QueryerExpression.eq("status", "ACTIVE"),
 *         QueryerExpression.gte("age", 18)
 *     )
 * </pre>
 *
 * @see QueryerExpression
 * @see Logic.LogicEnum
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryerPredicate {
    /**
     * 逻辑运算符，默认 AND
     */
    private Logic logic = Logic.LogicEnum.AND;

    /**
     * 条件表达式数组
     * <p>
     * 每个元素可以是：
     * <ul>
     *   <li>QueryerExpression - 简单条件</li>
     *   <li>QueryerPredicate - 嵌套条件组合</li>
     * </ul>
     */
    private Object[] exprs;

    /**
     * 判断条件组合是否为空
     *
     * @return true 表示没有条件表达式，false 表示有条件表达式
     */
    public boolean isEmpty() {
        return exprs == null || exprs.length == 0;
    }

    /**
     * 创建默认条件组合（AND 逻辑）
     *
     * @param exprs 条件表达式数组，可以是 QueryerExpression 或 QueryerPredicate
     * @return QueryerPredicate 实例，使用 AND 逻辑组合所有条件
     */
    public static QueryerPredicate of(Object... exprs) {
        QueryerPredicate p = new QueryerPredicate();
        p.logic = Logic.LogicEnum.AND;
        p.exprs = exprs;
        return p;
    }

    /**
     * 创建 AND 逻辑条件组合
     *
     * @param exprs 条件表达式数组，所有条件必须同时满足
     * @return QueryerPredicate 实例，使用 AND 逻辑组合
     */
    public static QueryerPredicate and(Object... exprs) {
        QueryerPredicate p = new QueryerPredicate();
        p.logic = Logic.LogicEnum.AND;
        p.exprs = exprs;
        return p;
    }

    /**
     * 创建 OR 逻辑条件组合
     *
     * @param exprs 条件表达式数组，满足任一条件即可
     * @return QueryerPredicate 实例，使用 OR 逻辑组合
     */
    public static QueryerPredicate or(Object... exprs) {
        QueryerPredicate p = new QueryerPredicate();
        p.logic = Logic.LogicEnum.OR;
        p.exprs = exprs;
        return p;
    }

    /**
     * 创建 NOT 逻辑条件组合（对当前条件取反）
     *
     * @return 新的 QueryerPredicate 实例，表示对当前条件的否定
     */
    public QueryerPredicate not() {
        QueryerPredicate p = new QueryerPredicate();
        p.logic = Logic.LogicEnum.NOT;
        p.exprs = new Object[]{this};
        return p;
    }
}
