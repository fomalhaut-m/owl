package com.vex.query.criteria.jpa;

import com.vex.query.criteria.*;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JPA Criteria 查询构建器
 * <p>
 * 将 {@link VexQueryCriteria} 转换为 JPA 的 {@link CriteriaQuery}，
 * 支持动态查询条件、字段选择、排序等功能。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><b>字段选择</b>：支持 select（包含）和 exclude（排除）字段</li>
 *   <li><b>过滤条件</b>：支持多种操作符（eq, gt, lt, in, between 等）</li>
 *   <li><b>排序</b>：支持多字段升序/降序排序</li>
 *   <li><b>类型安全</b>：在构建时进行严格的类型校验</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 创建查询条件
 * VexQueryCriteria criteria = VexQueryCriteria.builder()
 *     .filter(VexPredicate.and(
 *         VexExpression.gt("age", 18),
 *         VexExpression.exp("name", "%John%")
 *     ))
 *     .orderBy(VexSortOrder.desc("createTime"))
 *     .build();
 *
 * // 2. 构建 JPA 查询
 * CriteriaQuery<User> query = CriteriaQueryJpaBuilder.buildQuery(
 *     User.class, criteria, entityManager.getCriteriaBuilder()
 * );
 *
 * // 3. 执行查询
 * List<User> users = entityManager.createQuery(query).getResultList();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author Lingma Team
 * @since 1.0
 */
public class CriteriaQueryJpaBuilder<T> {

    private final Class<T> entityClass;
    private Root<T> root;
    private CriteriaQuery<T> query;

    /**
     * 私有构造函数，通过静态工厂方法创建实例
     *
     * @param entityClass 实体类类型
     */
    private CriteriaQueryJpaBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 为指定实体类创建查询构建器
     *
     * @param entityClass 实体类类型
     * @param <T>         实体类型
     * @return 查询构建器实例
     */
    public static <T> CriteriaQueryJpaBuilder<T> forEntity(Class<T> entityClass) {
        return new CriteriaQueryJpaBuilder<>(entityClass);
    }

    /**
     * 初始化 CriteriaQuery
     *
     * @param cb JPA CriteriaBuilder
     * @return 当前构建器实例（支持链式调用）
     */
    public CriteriaQueryJpaBuilder<T> from(jakarta.persistence.criteria.CriteriaBuilder cb) {
        this.query = cb.createQuery(entityClass);
        return this;
    }

    /**
     * 获取查询根对象
     *
     * @return Root 对象
     */
    public Root<T> getRoot() {
        return root;
    }

    /**
     * 获取构建好的 CriteriaQuery
     *
     * @return CriteriaQuery 对象
     */
    public CriteriaQuery<T> getQuery() {
        return query;
    }

    /**
     * 应用查询条件到 CriteriaQuery
     * <p>
     * 依次应用字段选择、过滤条件和排序规则。
     * </p>
     *
     * @param criteria 查询条件对象
     * @param cb       JPA CriteriaBuilder
     * @return 当前构建器实例（支持链式调用）
     */
    public CriteriaQueryJpaBuilder<T> apply(VexQueryCriteria criteria, jakarta.persistence.criteria.CriteriaBuilder cb) {
        this.root = query.from(entityClass);

        applySelect(criteria.getSelect(), criteria.getExclude());
        applyFilter(criteria.getFilter(), cb);
        applyOrderBy(criteria.getOrderBy(), cb);

        return this;
    }

    /**
     * 应用字段选择规则
     * <p>
     * 支持两种模式：
     * <ul>
     *   <li><b>select 模式</b>：只查询指定的字段</li>
     *   <li><b>exclude 模式</b>：查询除指定字段外的所有字段</li>
     * </ul>
     * 如果两者都为空，则查询所有字段。
     * </p>
     *
     * @param select  要包含的字段列表
     * @param exclude 要排除的字段列表
     */
    private void applySelect(String[] select, String[] exclude) {
        if ((select == null || select.length == 0) && (exclude == null || exclude.length == 0)) {
            query.select(root);
            return;
        }

        List<Selection<T>> selections = new ArrayList<>();

        if (select != null && select.length > 0) {
            for (String field : select) {
                selections.add(root.get(field));
            }
        }

        if (exclude != null && exclude.length > 0) {
            root.getModel().getAttributes().forEach(attr -> {
                String attrName = attr.getName();
                if (!Arrays.asList(exclude).contains(attrName)) {
                    if (select == null || select.length == 0) {
                        selections.add(root.get(attrName));
                    } else if (!Arrays.asList(select).contains(attrName)) {
                        selections.add(root.get(attrName));
                    }
                }
            });
        }

        if (selections.isEmpty()) {
            query.select(root);
        } else {
            query.multiselect(selections.toArray(new Selection[0]));
        }
    }

    /**
     * 应用过滤条件
     * <p>
     * 将 VexPredicate 转换为 JPA Predicate，并设置到查询的 where 子句中。
     * 如果过滤条件为空，则不添加 where 子句。
     * </p>
     *
     * @param filter 过滤条件
     * @param cb     JPA CriteriaBuilder
     */
    private void applyFilter(VexCriterion filter, jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (filter == null || filter.checkEmpty()) {
            query.where();
            return;
        }
        Predicate predicate = buildPredicate(filter, cb);
        query.where(predicate);
    }

    /**
     * 递归构建 Predicate
     * <p>
     * 支持嵌套的逻辑组合（AND/OR/NOT）。
     * </p>
     *
     * @param criteriaPredicate 谓词条件
     * @param cb                JPA CriteriaBuilder
     * @return 构建好的 Predicate
     */
    private Predicate buildPredicate(VexPredicate criteriaPredicate, jakarta.persistence.criteria.CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        for (VexCriterion criterion : criteriaPredicate.getExpressions()) {
            predicates.add(buildPredicate(criterion, cb));
        }

        if (predicates.isEmpty()) {
            return cb.conjunction();
        }

        return switch (criteriaPredicate.getLogic()) {
            case and -> cb.and(predicates.toArray(new Predicate[0]));
            case or -> cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 构建单个条件的 Predicate
     * <p>
     * 支持两种类型：
     * <ul>
     *   <li><b>VexExpression</b>：单个表达式（如 age > 18）</li>
     *   <li><b>VexPredicate</b>：嵌套的谓词组合（如 AND/OR）</li>
     * </ul>
     * </p>
     *
     * @param criterion 条件对象
     * @param cb        JPA CriteriaBuilder
     * @return 构建好的 Predicate
     */
    private Predicate buildPredicate(VexCriterion criterion, jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (criterion instanceof VexExpression expr) {
            return buildExpression(expr, cb);
        } else if (criterion instanceof VexPredicate pred) {
            return buildPredicate(pred, cb);
        }
        throw new IllegalArgumentException("Unknown criterion type: " + criterion.getClass().getName());
    }

    /**
     * 构建表达式的 Predicate
     * <p>
     * 根据操作符类型，调用相应的 JPA CriteriaBuilder 方法构建查询条件。
     * 在构建前会先进行严格的类型校验。
     * </p>
     *
     * @param expr 表达式对象
     * @param cb   JPA CriteriaBuilder
     * @return 构建好的 Predicate
     */
    
    private Predicate buildExpression(VexExpression expr, jakarta.persistence.criteria.CriteriaBuilder cb) {
        String fieldName = expr.getField();
        VexOperator op = expr.getOp();
        Object value = expr.getValue();
        
        // 使用验证器校验表达式的合法性
        VexExpressionJpaValidator.validate(fieldName, op, value);

        final Path<Object> objectPath = objectPath(fieldName);

        return switch (op) {
            case eq -> cb.equal(objectPath, value);
            case neq -> cb.notEqual(objectPath, value);
            case in -> buildInPredicate(objectPath, value);
            case not_in -> cb.not(buildInPredicate(objectPath, value));
            case is_null -> cb.isNull(objectPath);
            case is_not_null -> cb.isNotNull(objectPath);
            case gt -> buildGreaterThan(objectPath, value, cb);
            case gte -> buildGreaterThanOrEqualTo(objectPath, value, cb);
            case lt -> buildLessThan(objectPath, value, cb);
            case lte -> buildLessThanOrEqualTo(objectPath, value, cb);
            case exp -> buildLikePredicate(objectPath, value, cb, false);
            case not_exp -> buildLikePredicate(objectPath, value, cb, true);
            case between -> buildBetweenPredicate(objectPath, value, cb);
        };
    }

    /**
     * 获取字段的 Path 对象
     * <p>
     * 捕获 JPA 可能抛出的异常，并转换为更友好的错误信息。
     * </p>
     *
     * @param fieldName 字段名
     * @return 字段的 Path 对象
     * @throws IllegalArgumentException 当字段不存在时抛出
     * @throws IllegalStateException    当字段是基本类型无法导航时抛出
     */
    private Path<Object> objectPath(String fieldName) {
        final Path<Object> objectPath;
        try {
            objectPath = root.get(fieldName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Field '" + fieldName + "' does not exist in entity '" +
                entityClass.getSimpleName() + "'", e);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Field '" + fieldName + "' corresponds to a basic type and cannot be navigated", e);
        }
        return objectPath;
    }


    /**
     * 构建 IN 条件的 Predicate
     * <p>
     * 支持 Iterable（如 List、Set）和数组类型（如 Integer[]、String[]）。
     * </p>
     *
     * @param path  字段路径
     * @param value 值（Iterable 或数组类型）
     * @return IN Predicate
     */
    @SuppressWarnings("unchecked")
    private Predicate buildInPredicate(Path<?> path, Object value) {
        // 值已在 VexExpressionJpaValidator 中校验过，这里处理不同类型
        if (value instanceof Iterable) {
            return path.in((Iterable<?>) value);
        } else if (value.getClass().isArray()) {
            // 将数组转换为 List
            if (value instanceof Object[]) {
                return path.in(java.util.Arrays.asList((Object[]) value));
            } else if (value instanceof int[]) {
                return path.in(java.util.Arrays.stream((int[]) value).boxed().toList());
            } else if (value instanceof long[]) {
                return path.in(java.util.Arrays.stream((long[]) value).boxed().toList());
            } else if (value instanceof double[]) {
                return path.in(java.util.Arrays.stream((double[]) value).boxed().toList());
            } else {
                // 其他基本类型数组，使用反射转换
                int length = java.lang.reflect.Array.getLength(value);
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(java.lang.reflect.Array.get(value, i));
                }
                return path.in(list);
            }
        } else {
            throw new IllegalArgumentException("IN operator requires an Iterable or array value");
        }
    }

    /**
     * 构建大于（>）条件的 Predicate
     *
     * @param path 字段路径
     * @param value 比较值（必须实现 Comparable）
     * @param cb   JPA CriteriaBuilder
     * @return greater-than Predicate
     */
    
    private Predicate buildGreaterThan(Path<?> path, Object value, jakarta.persistence.criteria.CriteriaBuilder cb) {
        return cb.greaterThan((Path<? extends Comparable>) path, (Comparable) value);
    }

    /**
     * 构建大于等于（>=）条件的 Predicate
     *
     * @param path 字段路径
     * @param value 比较值（必须实现 Comparable）
     * @param cb   JPA CriteriaBuilder
     * @return greater-than-or-equal-to Predicate
     */
    
    private Predicate buildGreaterThanOrEqualTo(Path<?> path, Object value, jakarta.persistence.criteria.CriteriaBuilder cb) {
        return cb.greaterThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) value);
    }

    /**
     * 构建小于（<）条件的 Predicate
     *
     * @param path 字段路径
     * @param value 比较值（必须实现 Comparable）
     * @param cb   JPA CriteriaBuilder
     * @return less-than Predicate
     */
    
    private Predicate buildLessThan(Path<?> path, Object value, jakarta.persistence.criteria.CriteriaBuilder cb) {
        return cb.lessThan((Path<? extends Comparable>) path, (Comparable) value);
    }

    /**
     * 构建小于等于（<=）条件的 Predicate
     *
     * @param path 字段路径
     * @param value 比较值（必须实现 Comparable）
     * @param cb   JPA CriteriaBuilder
     * @return less-than-or-equal-to Predicate
     */
    
    private Predicate buildLessThanOrEqualTo(Path<?> path, Object value, jakarta.persistence.criteria.CriteriaBuilder cb) {
        return cb.lessThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) value);
    }

    /**
     * 构建 LIKE 条件的 Predicate
     * <p>
     * 支持 SQL LIKE 通配符：
     * <ul>
     *   <li><b>%</b>：匹配零个或多个字符</li>
     *   <li><b>_</b>：匹配单个字符</li>
     * </ul>
     * </p>
     *
     * @param path   字段路径（必须是 String 类型）
     * @param value  匹配模式（String 类型）
     * @param cb     JPA CriteriaBuilder
     * @param negate 是否为 NOT LIKE
     * @return LIKE 或 NOT LIKE Predicate
     */
    
    private Predicate buildLikePredicate(Path<?> path, Object value, jakarta.persistence.criteria.CriteriaBuilder cb, boolean negate) {
        Expression<String> stringPath = (Expression<String>) path;
        Predicate likePredicate = cb.like(stringPath, (String) value);
        return negate ? cb.not(likePredicate) : likePredicate;
    }

    /**
     * 构建 BETWEEN 条件的 Predicate
     * <p>
     * 用于范围查询，如：age BETWEEN 18 AND 60
     * </p>
     *
     * @param path  字段路径
     * @param value 包含两个元素的数组 [起始值, 结束值]
     * @param cb    JPA CriteriaBuilder
     * @return BETWEEN Predicate
     */
    
    private Predicate buildBetweenPredicate(Path<?> path, Object value, jakarta.persistence.criteria.CriteriaBuilder cb) {
        Object[] array = (Object[]) value;
        return cb.between((Path<? extends Comparable>) path, (Comparable) array[0], (Comparable) array[1]);
    }


    /**
     * 应用排序规则
     * <p>
     * 支持多字段排序，可以指定升序（ASC）或降序（DESC）。
     * </p>
     *
     * @param orders 排序条件数组
     * @param cb     JPA CriteriaBuilder
     */
    private void applyOrderBy(VexSortOrder[] orders, jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (orders == null || orders.length == 0) {
            return;
        }

        List<Order> orderList = new ArrayList<>();

        for (VexSortOrder order : orders) {
            String propertyName = order.getProperty();
            Path<?> path;
            try {
                path = root.get(propertyName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Order by field '" + propertyName + "' does not exist in entity '" + 
                    entityClass.getSimpleName() + "'", e);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Order by field '" + propertyName + "' corresponds to a basic type and cannot be navigated", e);
            }
            
            if (order.checkDesc()) {
                orderList.add(cb.desc(path));
            } else {
                orderList.add(cb.asc(path));
            }
        }

        query.orderBy(orderList);
    }

    /**
     * 静态工厂方法：构建完整的 CriteriaQuery
     * <p>
     * 这是一个便捷方法，封装了创建构建器、初始化、应用条件的完整流程。
     * </p>
     *
     * @param entityClass 实体类类型
     * @param criteria    查询条件对象
     * @param cb          JPA CriteriaBuilder
     * @param <T>         实体类型
     * @return 构建好的 CriteriaQuery
     *
     * @example
     * <pre>{@code
     * CriteriaQuery<User> query = CriteriaQueryJpaBuilder.buildQuery(
     *     User.class,
     *     criteria,
     *     entityManager.getCriteriaBuilder()
     * );
     * }</pre>
     */
    public static <T> CriteriaQuery<T> buildQuery(
            Class<T> entityClass,
            VexQueryCriteria criteria,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        return forEntity(entityClass)
                .from(cb)
                .apply(criteria, cb)
                .getQuery();
    }
}