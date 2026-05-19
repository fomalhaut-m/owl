package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("toString 方法测试")
class ToStringTest {

    @Test
    @DisplayName("VexExpression toString")
    void testVexExpressionToString() {
        VexExpression expr = VexExpression.eq("status", "ACTIVE");
        System.out.println("表达式: " + expr);
        
        VexExpression betweenExpr = VexExpression.between("age", 18, 65);
        System.out.println("范围表达式: " + betweenExpr);
        
        VexExpression nullExpr = VexExpression.isNull("deletedAt");
        System.out.println("NULL表达式: " + nullExpr);
    }

    @Test
    @DisplayName("VexPredicate toString")
    void testVexPredicateToString() {
        VexPredicate predicate = VexPredicate.and(
            VexExpression.eq("status", "ACTIVE"),
            VexExpression.gte("age", 18)
        );
        System.out.println("AND条件: " + predicate);
        
        VexPredicate orPredicate = VexPredicate.or(
            VexExpression.exp("username", "%张%"),
            VexExpression.exp("username", "%李%")
        );
        System.out.println("OR条件: " + orPredicate);
    }

    @Test
    @DisplayName("嵌套条件 toString")
    void testNestedPredicateToString() {
        VexPredicate nested = VexPredicate.and(
            VexExpression.eq("status", "ACTIVE"),
            VexPredicate.or(
                VexExpression.exp("username", "%张%"),
                VexExpression.exp("username", "%李%")
            )
        );
        System.out.println("嵌套条件: " + nested);
    }

    @Test
    @DisplayName("VexSortOrder toString")
    void testSortOrderToString() {
        VexSortOrder asc = VexSortOrder.asc("createTime");
        System.out.println("升序: " + asc);
        
        VexSortOrder desc = VexSortOrder.desc("updateTime");
        System.out.println("降序: " + desc);
    }

    @Test
    @DisplayName("VexPageRequest toString")
    void testPageRequestToString() {
        VexPageRequest paging = VexPageRequest.of(0, 20);
        System.out.println("分页: " + paging);
    }

    @Test
    @DisplayName("VexQueryCriteria toString")
    void testQueryCriteriaToString() {
        VexQueryCriteria query = VexQueryCriteria.of()
            .select("id", "username", "email")
            .exclude("password")
            .filter(VexPredicate.and(
                VexExpression.eq("status", "ACTIVE")
            ))
            .orderBy(VexSortOrder.desc("createTime"))
            .paging(VexPageRequest.of(0, 20));
        
        System.out.println("完整查询: " + query);
    }

    @Test
    @DisplayName("空查询 toString")
    void testEmptyQueryToString() {
        VexQueryCriteria query = VexQueryCriteria.of();
        System.out.println("空查询: " + query);
    }
}