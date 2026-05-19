package com.vex.query.criteria.jpa;

import com.vex.query.criteria.VexOperator;

/**
 * Vex 表达式 JPA 校验器
 * <p>
 * 用于在构建 JPA 查询条件时，验证表达式的字段名、操作符和值的类型正确性。
 * 该验证器确保在编译期就能捕获类型错误，避免运行时异常。
 * </p>
 *
 * <h3>校验规则：</h3>
 * <ul>
 *   <li><b>eq/neq</b>：相等/不相等操作，值可以为 null（用于查询 null 值）</li>
 *   <li><b>gt/gte/lt/lte</b>：比较操作，值必须实现 {@link Comparable} 接口</li>
 *   <li><b>exp/not_exp</b>：模糊匹配操作，值必须为 {@link String} 类型</li>
 *   <li><b>in/not_in</b>：集合操作，值必须为 {@link Iterable} 类型</li>
 *   <li><b>between</b>：范围操作，值必须为包含 2 个元素的 Object[] 数组，且元素实现 {@link Comparable}</li>
 *   <li><b>is_null/is_not_null</b>：空值判断操作，不需要值（必须为 null）</li>
 * </ul>
 *
 * @author Lingma Team
 * @since 1.0
 */
public class VexExpressionJpaValidator {

    /**
     * 验证 Vex 表达式的合法性
     * <p>
     * 根据操作符类型，对字段名和值进行严格的类型校验。
     * 如果校验失败，将抛出 {@link IllegalArgumentException} 异常。
     * </p>
     *
     * @param field 字段名，不能为 null 或空白字符串
     * @param op    操作符，决定值的类型要求
     * @param value 值，根据操作符不同有不同的类型要求
     * @throws IllegalArgumentException 当字段名为空、值类型不匹配或操作符不支持时抛出
     *
     * @example
     * <pre>{@code
     * // ✅ 正确的用法
     * validate("age", VexOperator.gt, 18);              // Integer 实现 Comparable
     * validate("name", VexOperator.exp, "%John%");      // String 类型
     * validate("id", VexOperator.in, List.of(1, 2, 3)); // Iterable 类型
     * validate("age", VexOperator.between, new Object[]{18, 60}); // Object[] with 2 elements
     *
     * // ❌ 错误的用法 - 会抛出异常
     * validate("name", VexOperator.gt, "abc");          // String 不是数值类型
     * validate("age", VexOperator.exp, 123);            // 需要 String 类型
     * validate("id", VexOperator.in, "1,2,3");         // 需要 Iterable 类型
     * }</pre>
     */
    public static void validate(String field, VexOperator op, Object value) {
        // 校验字段名不能为空
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("Field name cannot be null or blank");
        }

        // 根据不同操作符校验值的类型
        switch (op) {
            case eq, neq -> {
                // 相等/不相等操作，值可以为 null（用于查询 null 值）
            }
            case gt, gte, lt, lte -> {
                // 比较操作需要 Comparable 类型的值（如 Number、Date、String 等）
                if (value == null) {
                    throw new IllegalArgumentException(op + " operator requires a non-null Comparable value");
                }
                if (!(value instanceof Comparable)) {
                    throw new IllegalArgumentException(op + " operator requires a Comparable value, got: " +
                        value.getClass().getName());
                }
            }
            case exp, not_exp -> {
                // LIKE 操作需要 String 类型（支持 % 和 _ 通配符）
                if (value == null) {
                    throw new IllegalArgumentException(op + " operator requires a non-null String value");
                }
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException(op + " operator requires a String value, got: " +
                        value.getClass().getName());
                }
            }
            case in, not_in -> {
                // IN 操作需要 Iterable 类型或数组（如 List、Set、Integer[]、String[] 等）
                if (value == null) {
                    throw new IllegalArgumentException(op + " operator requires a non-null Iterable or array value");
                }
                if (!(value instanceof Iterable) && !value.getClass().isArray()) {
                    throw new IllegalArgumentException(op + " operator requires an Iterable or array value, got: " +
                        value.getClass().getName());
                }
            }
            case between -> {
                // BETWEEN 操作需要包含 2 个元素的数组（起始值和结束值）
                if (value == null) {
                    throw new IllegalArgumentException("BETWEEN operator requires a non-null array with 2 elements");
                }
                if (!(value instanceof Object[] arr)) {
                    throw new IllegalArgumentException("BETWEEN operator requires an Object[] array, got: " +
                        value.getClass().getName());
                }
                if (arr.length != 2) {
                    throw new IllegalArgumentException("BETWEEN operator requires exactly 2 values, got: " + arr.length);
                }
                // 验证数组中的元素是否为 Comparable
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] != null && !(arr[i] instanceof Comparable)) {
                        throw new IllegalArgumentException("BETWEEN operator element at index " + i +
                            " must be Comparable, got: " + arr[i].getClass().getName());
                    }
                }
            }
            case is_null, is_not_null -> {
                // IS NULL / IS NOT NULL 操作不需要值
                if (value != null) {
                    throw new IllegalArgumentException(op + " operator does not require a value");
                }
            }
            default -> {
                throw new IllegalArgumentException("Unsupported operator: " + op);
            }
        }
    }
}