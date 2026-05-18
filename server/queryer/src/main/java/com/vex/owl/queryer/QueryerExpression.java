package com.vex.owl.queryer;

import lombok.Data;

/**
 * 表达式
 * <p>
 * 表示单个条件表达式，包含字段名、操作符和值
 * <p>
 * 示例：
 * <pre>
 *     QueryerExpression.eq("status", "ACTIVE")
 *     QueryerExpression.exp("username", "john")
 *     QueryerExpression.between("age", 18, 65)
 * </pre>
 *
 * @see QueryerPredicate
 * @see Operator.OperatorEnum
 */
@Data
public class QueryerExpression {
    /**
     * 字段名
     */
    private String field;

    /**
     * 操作符
     */
    private Operator op;

    /**
     * 值
     */
    private Object value;

    /**
     * 创建自定义表达式
     *
     * @param field 字段名
     * @param op    操作符
     * @param value 值
     * @return QueryerExpression 实例
     */
    public static QueryerExpression of(String field, Operator op, Object value) {
        QueryerExpression expr = new QueryerExpression();
        expr.field = field;
        expr.op = op;
        expr.value = value;
        return expr;
    }

    /**
     * 创建等于条件表达式
     *
     * @param field 字段名
     * @param value 要匹配的值
     * @return QueryerExpression 实例，表示 field = value
     */
    public static QueryerExpression eq(String field, Object value) {
        return of(field, Operator.OperatorEnum.EQ, value);
    }

    /**
     * 创建不等于条件表达式
     *
     * @param field 字段名
     * @param value 要排除的值
     * @return QueryerExpression 实例，表示 field != value
     */
    public static QueryerExpression neq(String field, Object value) {
        return of(field, Operator.OperatorEnum.NEQ, value);
    }

    /**
     * 创建大于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return QueryerExpression 实例，表示 field > value
     */
    public static QueryerExpression gt(String field, Object value) {
        return of(field, Operator.OperatorEnum.GT, value);
    }

    /**
     * 创建大于等于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return QueryerExpression 实例，表示 field >= value
     */
    public static QueryerExpression gte(String field, Object value) {
        return of(field, Operator.OperatorEnum.GTE, value);
    }

    /**
     * 创建小于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return QueryerExpression 实例，表示 field < value
     */
    public static QueryerExpression lt(String field, Object value) {
        return of(field, Operator.OperatorEnum.LT, value);
    }

    /**
     * 创建小于等于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return QueryerExpression 实例，表示 field <= value
     */
    public static QueryerExpression lte(String field, Object value) {
        return of(field, Operator.OperatorEnum.LTE, value);
    }

    /**
     * 创建模糊匹配条件表达式
     *
     * @param field 字段名
     * @param value 匹配模式（支持通配符）
     * @return QueryerExpression 实例，表示 field LIKE value
     */
    public static QueryerExpression exp(String field, Object value) {
        return of(field, Operator.OperatorEnum.EXP, value);
    }

    /**
     * 创建 IN 条件表达式
     *
     * @param field 字段名
     * @param value 值集合（数组或列表）
     * @return QueryerExpression 实例，表示 field IN (values)
     */
    public static QueryerExpression in(String field, Object value) {
        return of(field, Operator.OperatorEnum.IN, value);
    }

    /**
     * 创建范围条件表达式
     *
     * @param field 字段名
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return QueryerExpression 实例，表示 min <= field <= max
     */
    public static QueryerExpression between(String field, Object min, Object max) {
        return of(field, Operator.OperatorEnum.BETWEEN, new Object[]{min, max});
    }

    /**
     * 创建为空条件表达式
     *
     * @param field 字段名
     * @return QueryerExpression 实例，表示 field IS NULL
     */
    public static QueryerExpression isNull(String field) {
        return of(field, Operator.OperatorEnum.IS_NULL, null);
    }

    /**
     * 创建不为空条件表达式
     *
     * @param field 字段名
     * @return QueryerExpression 实例，表示 field IS NOT NULL
     */
    public static QueryerExpression isNotNull(String field) {
        return of(field, Operator.OperatorEnum.IS_NOT_NULL, null);
    }
}
