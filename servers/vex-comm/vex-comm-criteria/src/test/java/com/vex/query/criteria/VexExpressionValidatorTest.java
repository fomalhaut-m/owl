package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("表达式校验测试")
class VexExpressionValidatorTest {

    @Nested
    @DisplayName("字段名校验")
    class FieldValidation {

        @Test
        @DisplayName("NULL 字段名 - 非法")
        void testNullFieldName() {
            assertThrows(IllegalArgumentException.class,
                () -> VexExpressionValidator.validate(null, VexOperator.eq, "value"));
        }

        @Test
        @DisplayName("空白字段名 - 非法")
        void testBlankFieldName() {
            assertThrows(IllegalArgumentException.class,
                () -> VexExpressionValidator.validate("  ", VexOperator.eq, "value"));
        }
    }

    @Nested
    @DisplayName("比较操作符校验")
    class ComparisonValidation {

        @Test
        @DisplayName("EQ 操作符需要非空值")
        void testEqRequiresValue() {
            assertThrows(IllegalArgumentException.class,
                () -> VexExpressionValidator.validate("field", VexOperator.eq, null));
        }

        @Test
        @DisplayName("GT 操作符需要非空值")
        void testGtRequiresValue() {
            assertThrows(IllegalArgumentException.class,
                () -> VexExpressionValidator.validate("field", VexOperator.gt, null));
        }
    }

    @Nested
    @DisplayName("BETWEEN 操作符校验")
    class BetweenValidation {

        @Test
        @DisplayName("BETWEEN 需要数组值")
        void testBetweenRequiresArray() {
            assertThrows(IllegalArgumentException.class,
                () -> VexExpressionValidator.validate("field", VexOperator.between, null));
        }

        @Test
        @DisplayName("BETWEEN 需要至少2个值")
        void testBetweenRequiresTwoValues() {
            assertThrows(IllegalArgumentException.class,
                () -> VexExpressionValidator.validate("field", VexOperator.between, new Object[]{1}));
        }
    }

    @Nested
    @DisplayName("IN/NOT_IN 操作符校验")
    class InValidation {

        @Test
        @DisplayName("IN 操作符需要值")
        void testInRequiresValue() {
            assertThrows(IllegalArgumentException.class,
                () -> VexExpressionValidator.validate("field", VexOperator.in, null));
        }
    }

    @Nested
    @DisplayName("IS_NULL/IS_NOT_NULL 操作符校验")
    class NullCheckValidation {

        @Test
        @DisplayName("IS_NULL 不需要值")
        void testIsNullNoValue() {
            assertDoesNotThrow(() ->
                VexExpressionValidator.validate("field", VexOperator.is_null, null));
        }

        @Test
        @DisplayName("IS_NOT_NULL 不需要值")
        void testIsNotNullNoValue() {
            assertDoesNotThrow(() ->
                VexExpressionValidator.validate("field", VexOperator.is_not_null, null));
        }
    }
}