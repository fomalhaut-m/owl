package com.vex.owl.queryer;

import lombok.Data;

/**
 * 查询条件
 * <p>
 * 用于构建复杂的查询条件，支持字段选择、条件过滤、排序和分页
 * <p>
 * 示例：
 * <pre>
 *     Queryer.of()
 *         .select("id", "username", "email")
 *         .exclude("password", "salt")
 *         .filter(QueryerPredicate.and(
 *             QueryerExpression.exp("username", "john"),
 *             QueryerExpression.in("status", new String[]{"ACTIVE", "PENDING"})
 *         ))
 *         .orderBy(QueryerSort.desc("createTime"))
 *         .paging(QueryerPageable.of(0, 20))
 * </pre>
 *
 * @see QueryerExpression
 * @see QueryerPredicate
 * @see QueryerSort
 * @see QueryerPageable
 */
@Data
public class Queryer {
    /**
     * 要查询的字段列表
     */
    private String[] select;

    /**
     * 要排除的字段列表
     */
    private String[] exclude;

    /**
     * 查询条件组合
     */
    private QueryerPredicate filter;

    /**
     * 排序规则
     */
    private QueryerSort[] orderBy;

    /**
     * 分页信息
     */
    private QueryerPageable paging;

    /**
     * 设置要查询的字段列表
     *
     * @param fields 字段名数组，指定需要返回的字段
     * @return 当前 Queryer 实例，支持链式调用
     */
    public Queryer select(String... fields) {
        this.select = fields;
        return this;
    }

    /**
     * 设置要排除的字段列表
     *
     * @param fields 字段名数组，指定不需要返回的字段
     * @return 当前 Queryer 实例，支持链式调用
     */
    public Queryer exclude(String... fields) {
        this.exclude = fields;
        return this;
    }

    /**
     * 设置查询条件过滤器
     *
     * @param filter 查询条件谓词，用于过滤数据
     * @return 当前 Queryer 实例，支持链式调用
     */
    public Queryer filter(QueryerPredicate filter) {
        this.filter = filter;
        return this;
    }

    /**
     * 设置排序规则
     *
     * @param sorts 排序规则数组，可指定多个字段的排序方式
     * @return 当前 Queryer 实例，支持链式调用
     */
    public Queryer orderBy(QueryerSort... sorts) {
        this.orderBy = sorts;
        return this;
    }

    /**
     * 设置分页信息
     *
     * @param paging 分页对象，包含页码和每页条数
     * @return 当前 Queryer 实例，支持链式调用
     */
    public Queryer paging(QueryerPageable paging) {
        this.paging = paging;
        return this;
    }

    /**
     * 创建空的查询对象
     *
     * @return 新的 Queryer 实例
     */
    public static Queryer of() {
        return new Queryer();
    }

    /**
     * 创建带字段选择的查询对象
     *
     * @param fields 要查询的字段名数组
     * @return 新的 Queryer 实例，已设置 select 字段
     */
    public static Queryer query(String... fields) {
        Queryer q = new Queryer();
        q.select = fields;
        return q;
    }
}
