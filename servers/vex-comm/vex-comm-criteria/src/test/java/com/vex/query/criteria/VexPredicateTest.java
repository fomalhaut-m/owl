package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("条件组合测试")
class VexPredicateTest {

    @Nested
    @DisplayName("创建条件组合")
    class CreatePredicate {

        @Test
        @DisplayName("创建 AND 组合")
        void testAnd() {
            VexPredicate predicate = VexPredicate.and(
                VexExpression.eq("status", "ACTIVE"),
                VexExpression.gte("age", 18)
            );

            assertEquals(VexLogic.and, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
        }

        @Test
        @DisplayName("创建 OR 组合")
        void testOr() {
            VexPredicate predicate = VexPredicate.or(
                VexExpression.eq("status", "ACTIVE"),
                VexExpression.eq("status", "PENDING")
            );

            assertEquals(VexLogic.or, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
        }

        @Test
        @DisplayName("创建 NOT 组合")
        void testNot() {
            VexPredicate original = VexPredicate.and(
                VexExpression.eq("status", "DELETED")
            );
            VexPredicate notPredicate = original.not();

            assertEquals(VexLogic.not, notPredicate.getLogic());
            assertEquals(1, notPredicate.getExpressions().length);
        }

        @Test
        @DisplayName("使用 of 方法创建默认 AND 组合")
        void testOf() {
            VexPredicate predicate = VexPredicate.of(
                VexExpression.eq("status", "ACTIVE")
            );

            assertEquals(VexLogic.and, predicate.getLogic());
            assertEquals(1, predicate.getExpressions().length);
        }
    }

    @Nested
    @DisplayName("嵌套条件组合")
    class NestedPredicate {

        @Test
        @DisplayName("创建嵌套的 AND-OR 组合")
        void testNestedAndOr() {
            VexPredicate predicate = VexPredicate.and(
                VexExpression.eq("status", "ACTIVE"),
                VexPredicate.or(
                    VexExpression.exp("username", "%张%"),
                    VexExpression.exp("username", "%李%")
                )
            );

            assertEquals(VexLogic.and, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
            
            VexCriterion secondExpr = predicate.getExpressions()[1];
            assertInstanceOf(VexPredicate.class, secondExpr);
            VexPredicate nestedPredicate = (VexPredicate) secondExpr;
            assertEquals(VexLogic.or, nestedPredicate.getLogic());
        }

        @Test
        @DisplayName("创建多层嵌套组合")
        void testMultiLevelNested() {
            VexPredicate predicate = VexPredicate.or(
                VexPredicate.and(
                    VexExpression.eq("type", "A"),
                    VexExpression.gt("value", 10)
                ),
                VexPredicate.and(
                    VexExpression.eq("type", "B"),
                    VexExpression.lt("value", 20)
                )
            );

            assertEquals(VexLogic.or, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
            
            for (VexCriterion expr : predicate.getExpressions()) {
                assertInstanceOf(VexPredicate.class, expr);
                VexPredicate nested = (VexPredicate) expr;
                assertEquals(VexLogic.and, nested.getLogic());
                assertEquals(2, nested.getExpressions().length);
            }
        }
    }

    @Nested
    @DisplayName("空值检查")
    class EmptyCheck {

        @Test
        @DisplayName("空表达式数组判断为空")
        void testEmptyWithNullArray() {
            VexPredicate predicate = new VexPredicate();
            assertTrue(predicate.checkEmpty());
        }

        @Test
        @DisplayName("空数组判断为空")
        void testEmptyWithEmptyArray() {
            VexPredicate predicate = VexPredicate.and();
            assertTrue(predicate.checkEmpty());
        }

        @Test
        @DisplayName("有表达式判断不为空")
        void testNotEmpty() {
            VexPredicate predicate = VexPredicate.and(
                VexExpression.eq("status", "ACTIVE")
            );
            assertFalse(predicate.checkEmpty());
        }
    }

    @Nested
    @DisplayName("实现 VexCriterion 接口")
    class ImplementInterface {

        @Test
        @DisplayName("非空谓词 checkEmpty 返回 false")
        void testCheckEmptyWhenNotEmpty() {
            VexPredicate predicate = VexPredicate.and(
                VexExpression.eq("status", "ACTIVE")
            );
            assertFalse(predicate.checkEmpty());
        }
    }
}