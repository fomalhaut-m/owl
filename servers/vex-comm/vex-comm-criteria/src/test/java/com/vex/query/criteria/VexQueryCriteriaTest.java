package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("查询条件测试")
class VexQueryCriteriaTest {

    @Nested
    @DisplayName("创建查询对象")
    class CreateQuery {

        @Test
        @DisplayName("创建空查询对象")
        void testOf() {
            VexQueryCriteria query = VexQueryCriteria.of();
            
            assertNotNull(query);
            assertNull(query.getSelect());
            assertNull(query.getExclude());
            assertNull(query.getFilter());
            assertNull(query.getOrderBy());
            assertNull(query.getPaging());
        }

        @Test
        @DisplayName("创建带字段选择的查询对象")
        void testQuery() {
            VexQueryCriteria query = VexQueryCriteria.query("id", "username", "email");
            
            assertNotNull(query);
            assertArrayEquals(new String[]{"id", "username", "email"}, query.getSelect());
        }
    }

    @Nested
    @DisplayName("链式调用")
    class ChainCall {

        @Test
        @DisplayName("设置选择字段")
        void testSelect() {
            VexQueryCriteria query = VexQueryCriteria.of().select("id", "name");
            
            assertArrayEquals(new String[]{"id", "name"}, query.getSelect());
        }

        @Test
        @DisplayName("设置排除字段")
        void testExclude() {
            VexQueryCriteria query = VexQueryCriteria.of().exclude("password", "salt");
            
            assertArrayEquals(new String[]{"password", "salt"}, query.getExclude());
        }

        @Test
        @DisplayName("设置过滤条件")
        void testFilter() {
            VexPredicate filter = VexPredicate.and(
                VexExpression.eq("status", "ACTIVE")
            );
            VexQueryCriteria query = VexQueryCriteria.of().filter(filter);
            
            assertEquals(filter, query.getFilter());
        }

        @Test
        @DisplayName("设置排序规则")
        void testOrderBy() {
            VexSortOrder sort1 = VexSortOrder.asc("createTime");
            VexSortOrder sort2 = VexSortOrder.desc("updateTime");
            VexQueryCriteria query = VexQueryCriteria.of().orderBy(sort1, sort2);
            
            assertEquals(2, query.getOrderBy().length);
            assertEquals(sort1, query.getOrderBy()[0]);
            assertEquals(sort2, query.getOrderBy()[1]);
        }

        @Test
        @DisplayName("设置分页信息")
        void testPaging() {
            VexPageRequest paging = VexPageRequest.of(0, 20);
            VexQueryCriteria query = VexQueryCriteria.of().paging(paging);
            
            assertEquals(paging, query.getPaging());
        }

        @Test
        @DisplayName("完整的链式调用")
        void testFullChain() {
            VexQueryCriteria query = VexQueryCriteria.of()
                .select("id", "username", "email")
                .exclude("password")
                .filter(VexPredicate.and(
                    VexExpression.eq("status", "ACTIVE")
                ))
                .orderBy(VexSortOrder.desc("createTime"))
                .paging(VexPageRequest.of(0, 20));

            assertNotNull(query.getSelect());
            assertNotNull(query.getExclude());
            assertNotNull(query.getFilter());
            assertNotNull(query.getOrderBy());
            assertNotNull(query.getPaging());
        }
    }

    @Nested
    @DisplayName("复杂查询场景")
    class ComplexQuery {

        @Test
        @DisplayName("构建用户搜索查询")
        void testUserSearchQuery() {
            VexQueryCriteria query = VexQueryCriteria.of()
                .select("id", "username", "email", "age")
                .exclude("password", "salt")
                .filter(VexPredicate.and(
                    VexExpression.exp("username", "%张%"),
                    VexExpression.gte("age", 18),
                    VexExpression.lte("age", 65),
                    VexPredicate.or(
                        VexExpression.eq("status", "ACTIVE"),
                        VexExpression.eq("status", "PENDING")
                    )
                ))
                .orderBy(
                    VexSortOrder.desc("createTime"),
                    VexSortOrder.asc("username")
                )
                .paging(VexPageRequest.of(0, 20));

            assertEquals(4, query.getSelect().length);
            assertEquals(2, query.getExclude().length);
            assertNotNull(query.getFilter());
            assertEquals(VexLogic.and, query.getFilter().getLogic());
            assertEquals(4, query.getFilter().getExpressions().length);
            assertEquals(2, query.getOrderBy().length);
        }
    }
}