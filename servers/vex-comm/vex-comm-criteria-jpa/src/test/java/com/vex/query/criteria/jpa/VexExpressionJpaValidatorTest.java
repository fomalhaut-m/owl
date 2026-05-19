package com.vex.query.criteria.jpa;

import com.vex.query.criteria.VexExpression;
import com.vex.query.criteria.VexOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vex 表达式 JPA 校验器单元测试
 * <p>
 * 测试 {@link VexExpressionJpaValidator} 的各种校验场景，确保所有操作符的类型校验正确。
 * </p>
 *
 * @author Lingma Team
 * @since 1.0
 */
@DisplayName("VexExpressionJpaValidator 单元测试")
class VexExpressionJpaValidatorTest {

    @Nested
    @DisplayName("字段名校验")
    class FieldNameValidation {

        @Test
        @DisplayName("字段名为 null 时应抛出异常")
        void testNullFieldName() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate(null, VexOperator.eq, "value")
            );
            assertEquals("Field name cannot be null or blank", exception.getMessage());
        }

        @Test
        @DisplayName("字段名为空字符串时应抛出异常")
        void testEmptyFieldName() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("", VexOperator.eq, "value")
            );
            assertEquals("Field name cannot be null or blank", exception.getMessage());
        }

        @Test
        @DisplayName("字段名为空白字符串时应抛出异常")
        void testBlankFieldName() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("   ", VexOperator.eq, "value")
            );
            assertEquals("Field name cannot be null or blank", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("相等操作符校验 (eq/neq)")
    class EqualityOperators {

        @Test
        @DisplayName("eq 操作符允许值为 null")
        void testEqWithNullValue() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("name", VexOperator.eq, null)
            );
        }

        @Test
        @DisplayName("eq 操作符允许任意类型的值")
        void testEqWithAnyType() {
            assertDoesNotThrow(() -> {
                VexExpressionJpaValidator.validate("age", VexOperator.eq, 18);
                VexExpressionJpaValidator.validate("name", VexOperator.eq, "John");
                VexExpressionJpaValidator.validate("active", VexOperator.eq, true);
            });
        }

        @Test
        @DisplayName("neq 操作符允许值为 null")
        void testNeqWithNullValue() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("name", VexOperator.neq, null)
            );
        }
    }

    @Nested
    @DisplayName("比较操作符校验 (gt/gte/lt/lte)")
    class ComparisonOperators {

        @Test
        @DisplayName("gt 操作符要求值非 null 且实现 Comparable")
        void testGtWithValidComparable() {
            assertDoesNotThrow(() -> {
                VexExpressionJpaValidator.validate("age", VexOperator.gt, 18);
                VexExpressionJpaValidator.validate("price", VexOperator.gt, 99.99);
                VexExpressionJpaValidator.validate("name", VexOperator.gt, "Alice");
                VexExpressionJpaValidator.validate("createTime", VexOperator.gt, new java.util.Date());
            });
        }

        @Test
        @DisplayName("gt 操作符值为 null 时应抛出异常")
        void testGtWithNullValue() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("age", VexOperator.gt, null)
            );
            assertTrue(exception.getMessage().contains("requires a non-null Comparable value"));
        }

        @Test
        @DisplayName("gt 操作符值不是 Comparable 时应抛出异常")
        void testGtWithNonComparable() {
            // 创建一个不实现 Comparable 的匿名类
            Object nonComparable = new Object() {};
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("field", VexOperator.gt, nonComparable)
            );
            assertTrue(exception.getMessage().contains("requires a Comparable value"));
        }

        @Test
        @DisplayName("gte 操作符校验规则与 gt 相同")
        void testGteValidation() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("age", VexOperator.gte, 18)
            );
        }

        @Test
        @DisplayName("lt 操作符校验规则与 gt 相同")
        void testLtValidation() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("age", VexOperator.lt, 60)
            );
        }

        @Test
        @DisplayName("lte 操作符校验规则与 gt 相同")
        void testLteValidation() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("age", VexOperator.lte, 60)
            );
        }
    }

    @Nested
    @DisplayName("模糊匹配操作符校验 (exp/not_exp)")
    class LikeOperators {

        @Test
        @DisplayName("exp 操作符要求值为 String 类型")
        void testExpWithValidString() {
            assertDoesNotThrow(() -> {
                VexExpressionJpaValidator.validate("name", VexOperator.exp, "%John%");
                VexExpressionJpaValidator.validate("email", VexOperator.exp, "_@example.com");
                VexExpressionJpaValidator.validate("phone", VexOperator.exp, "138____");
            });
        }

        @Test
        @DisplayName("exp 操作符值为 null 时应抛出异常")
        void testExpWithNullValue() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("name", VexOperator.exp, null)
            );
            assertTrue(exception.getMessage().contains("requires a non-null String value"));
        }

        @Test
        @DisplayName("exp 操作符值不是 String 时应抛出异常")
        void testExpWithNonString() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("name", VexOperator.exp, 123)
            );
            assertTrue(exception.getMessage().contains("requires a String value"));
        }

        @Test
        @DisplayName("not_exp 操作符校验规则与 exp 相同")
        void testNotExpValidation() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("name", VexOperator.not_exp, "%test%")
            );
        }
    }

    @Nested
    @DisplayName("集合操作符校验 (in/not_in)")
    class InOperators {

        @Test
        @DisplayName("in 操作符要求值为 Iterable 类型")
        void testInWithValidIterable() {
            assertDoesNotThrow(() -> {
                VexExpressionJpaValidator.validate("id", VexOperator.in, List.of(1, 2, 3));
                VexExpressionJpaValidator.validate("status", VexOperator.in, Set.of("active", "pending"));
            });
        }

        @Test
        @DisplayName("in 操作符支持数组类型")
        void testInWithArray() {
            assertDoesNotThrow(() -> {
                VexExpressionJpaValidator.validate("id", VexOperator.in, new Integer[]{1, 2, 3});
                VexExpressionJpaValidator.validate("name", VexOperator.in, new String[]{"Alice", "Bob"});
                VexExpressionJpaValidator.validate("score", VexOperator.in, new int[]{90, 85, 78});
            });
        }

        @Test
        @DisplayName("in 操作符值为 null 时应抛出异常")
        void testInWithNullValue() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("id", VexOperator.in, null)
            );
        }

        @Test
        @DisplayName("in 操作符值不是 Iterable 时应抛出异常")
        void testInWithNonIterable() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("id", VexOperator.in, "1,2,3")
            );
        }

        @Test
        @DisplayName("not_in 操作符校验规则与 in 相同")
        void testNotInValidation() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("id", VexOperator.not_in, List.of(1, 2, 3))
            );
        }
    }

    @Nested
    @DisplayName("范围操作符校验 (between)")
    class BetweenOperator {

        @Test
        @DisplayName("between 操作符要求值为包含2个元素的 Object[] 数组")
        void testBetweenWithValidArray() {
            assertDoesNotThrow(() -> {
                VexExpressionJpaValidator.validate("age", VexOperator.between, new Object[]{18, 60});
                VexExpressionJpaValidator.validate("price", VexOperator.between, new Object[]{10.5, 99.9});
                VexExpressionJpaValidator.validate("name", VexOperator.between, new Object[]{"A", "Z"});
            });
        }

        @Test
        @DisplayName("between 操作符值为 null 时应抛出异常")
        void testBetweenWithNullValue() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("age", VexOperator.between, null)
            );
            assertTrue(exception.getMessage().contains("requires a non-null array with 2 elements"));
        }

        @Test
        @DisplayName("between 操作符值不是 Object[] 时应抛出异常")
        void testBetweenWithNonArray() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("age", VexOperator.between, List.of(18, 60))
            );
            assertTrue(exception.getMessage().contains("requires an Object[] array"));
        }

        @Test
        @DisplayName("between 操作符数组元素不足2个时应抛出异常")
        void testBetweenWithLessThanTwoElements() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("age", VexOperator.between, new Object[]{18})
            );
            assertTrue(exception.getMessage().contains("requires exactly 2 values"));
        }

        @Test
        @DisplayName("between 操作符数组元素超过2个时应抛出异常")
        void testBetweenWithMoreThanTwoElements() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("age", VexOperator.between, new Object[]{18, 30, 60})
            );
            assertTrue(exception.getMessage().contains("requires exactly 2 values"));
        }

        @Test
        @DisplayName("between 操作符数组元素不是 Comparable 时应抛出异常")
        void testBetweenWithNonComparableElement() {
            Object nonComparable = new Object() {};
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("field", VexOperator.between, 
                    new Object[]{18, nonComparable})
            );
            assertTrue(exception.getMessage().contains("must be Comparable"));
        }

        @Test
        @DisplayName("between 操作符允许数组元素为 null")
        void testBetweenWithNullElement() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("field", VexOperator.between, new Object[]{null, 60})
            );
        }
    }

    @Nested
    @DisplayName("空值判断操作符校验 (is_null/is_not_null)")
    class NullOperators {

        @Test
        @DisplayName("is_null 操作符不需要值（值必须为 null）")
        void testIsNullWithNullValue() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("name", VexOperator.is_null, null)
            );
        }

        @Test
        @DisplayName("is_null 操作符有值时应抛出异常")
        void testIsNullWithNonNullValue() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("name", VexOperator.is_null, "value")
            );
            assertTrue(exception.getMessage().contains("does not require a value"));
        }

        @Test
        @DisplayName("is_not_null 操作符不需要值（值必须为 null）")
        void testIsNotNullWithNullValue() {
            assertDoesNotThrow(() -> 
                VexExpressionJpaValidator.validate("name", VexOperator.is_not_null, null)
            );
        }

        @Test
        @DisplayName("is_not_null 操作符有值时应抛出异常")
        void testIsNotNullWithNonNullValue() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VexExpressionJpaValidator.validate("name", VexOperator.is_not_null, "value")
            );
            assertTrue(exception.getMessage().contains("does not require a value"));
        }
    }

    @Nested
    @DisplayName("综合测试")
    class IntegrationTests {

        @Test
        @DisplayName("使用 VexExpression 静态方法创建表达式时应通过校验")
        void testVexExpressionStaticMethods() {
            assertAll("所有静态工厂方法",
                () -> VexExpression.eq("name", "John"),
                () -> VexExpression.neq("status", "deleted"),
                () -> VexExpression.gt("age", 18),
                () -> VexExpression.gte("score", 60),
                () -> VexExpression.lt("price", 100),
                () -> VexExpression.lte("quantity", 10),
                () -> VexExpression.exp("name", "%test%"),
                () -> VexExpression.notExp("email", "%spam%"),
                () -> VexExpression.in("id", List.of(1, 2, 3)),
                () -> VexExpression.notIn("status", Set.of("inactive")),
                () -> VexExpression.between("age", 18, 60),
                () -> VexExpression.isNull("deletedAt"),
                () -> VexExpression.isNotNull("createdAt")
            );
        }
    }
}
