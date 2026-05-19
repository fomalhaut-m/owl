package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("条件表达式测试")
class VexExpressionTest {

    @Nested
    @DisplayName("创建表达式")
    class CreateExpression {

        @Test
        @DisplayName("创建等于表达式")
        void testEq() {
            VexExpression expr = VexExpression.eq("status", "ACTIVE");
            
            assertEquals("status", expr.getField());
            assertEquals(VexOperator.eq, expr.getOp());
            assertEquals("ACTIVE", expr.getValue());
        }

        @Test
        @DisplayName("创建不等于表达式")
        void testNeq() {
            VexExpression expr = VexExpression.neq("status", "DELETED");
            
            assertEquals("status", expr.getField());
            assertEquals(VexOperator.neq, expr.getOp());
            assertEquals("DELETED", expr.getValue());
        }

        @Test
        @DisplayName("创建大于表达式")
        void testGt() {
            VexExpression expr = VexExpression.gt("age", 18);
            
            assertEquals("age", expr.getField());
            assertEquals(VexOperator.gt, expr.getOp());
            assertEquals(18, expr.getValue());
        }

        @Test
        @DisplayName("创建大于等于表达式")
        void testGte() {
            VexExpression expr = VexExpression.gte("score", 60);
            
            assertEquals("score", expr.getField());
            assertEquals(VexOperator.gte, expr.getOp());
            assertEquals(60, expr.getValue());
        }

        @Test
        @DisplayName("创建小于表达式")
        void testLt() {
            VexExpression expr = VexExpression.lt("age", 65);
            
            assertEquals("age", expr.getField());
            assertEquals(VexOperator.lt, expr.getOp());
            assertEquals(65, expr.getValue());
        }

        @Test
        @DisplayName("创建小于等于表达式")
        void testLte() {
            VexExpression expr = VexExpression.lte("price", 100);
            
            assertEquals("price", expr.getField());
            assertEquals(VexOperator.lte, expr.getOp());
            assertEquals(100, expr.getValue());
        }

        @Test
        @DisplayName("创建模糊匹配表达式")
        void testExp() {
            VexExpression expr = VexExpression.exp("username", "%张%");
            
            assertEquals("username", expr.getField());
            assertEquals(VexOperator.exp, expr.getOp());
            assertEquals("%张%", expr.getValue());
        }

        @Test
        @DisplayName("创建 IN 表达式")
        void testIn() {
            String[] values = {"ACTIVE", "PENDING"};
            VexExpression expr = VexExpression.in("status", values);
            
            assertEquals("status", expr.getField());
            assertEquals(VexOperator.in, expr.getOp());
            assertArrayEquals(values, (String[]) expr.getValue());
        }

        @Test
        @DisplayName("创建 BETWEEN 表达式")
        void testBetween() {
            VexExpression expr = VexExpression.between("age", 18, 65);
            
            assertEquals("age", expr.getField());
            assertEquals(VexOperator.between, expr.getOp());
            Object[] range = (Object[]) expr.getValue();
            assertEquals(18, range[0]);
            assertEquals(65, range[1]);
        }

        @Test
        @DisplayName("创建 IS NULL 表达式")
        void testIsNull() {
            VexExpression expr = VexExpression.isNull("deletedAt");
            
            assertEquals("deletedAt", expr.getField());
            assertEquals(VexOperator.is_null, expr.getOp());
            assertNull(expr.getValue());
        }

        @Test
        @DisplayName("创建 IS NOT NULL 表达式")
        void testIsNotNull() {
            VexExpression expr = VexExpression.isNotNull("email");
            
            assertEquals("email", expr.getField());
            assertEquals(VexOperator.is_not_null, expr.getOp());
            assertNull(expr.getValue());
        }

        @Test
        @DisplayName("使用 of 方法创建自定义表达式")
        void testOf() {
            VexExpression expr = VexExpression.of("field", VexOperator.eq, "value");
            
            assertEquals("field", expr.getField());
            assertEquals(VexOperator.eq, expr.getOp());
            assertEquals("value", expr.getValue());
        }
    }

    @Nested
    @DisplayName("实现 VexCriterion 接口")
    class ImplementInterface {

        @Test
        @DisplayName("表达式不为空")
        void testIsEmpty() {
            VexExpression expr = VexExpression.eq("status", "ACTIVE");
            assertFalse(expr.checkEmpty());
        }
    }
}